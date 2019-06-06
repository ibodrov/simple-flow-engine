package com.github.ibodrov.simpleflowengine;

/*-
 * *****
 * Simple Flow Engine
 * -----
 * Copyright (C) 2019 Ivan Bodrov
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.github.ibodrov.simpleflowengine.State.Status;
import com.github.ibodrov.simpleflowengine.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Executes flows consisting of series of {@link Command} elements.
 * Shouldn't be reused between executions.
 */
public class Runtime {

    private static final Logger log = LoggerFactory.getLogger(Runtime.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final RuntimeContext ctx = new RuntimeContextImpl();

    private boolean closed = false;

    /**
     * Starts a new process using the provided command as a starting point.
     *
     * @return the main thread state. The returned state object can be
     * saved and used later to {@link #resume(State, String)} the process.
     */
    public State start(Command cmd) throws Exception {
        log.info("start -> starting...");

        return once(() -> {
            // create the initial state
            State root = new State(ctx.nextStateId());
            root.getStack().push(cmd);

            // execute the root "thread"
            eval(root);

            handleErrors(root);
            cleanup(root);

            log.info("start -> done");
            return root;
        });
    }

    /**
     * Resumes the process from a specific point ("event").
     *
     * @return the updated state object.
     */
    public State resume(State root, String eventRef) throws Exception {
        log.info("resume ['{}'] -> starting...", eventRef);

        return once(() -> {
            // find whoever owns the event
            State owner = findOwner(root, eventRef);
            if (owner == null) {
                throw new IllegalStateException("EventRef not found: " + eventRef);
            }

            owner.getEventRefs().remove(eventRef);

            // wake the tree starting from the event's owner
            // but skip the root as it's going to run in the caller's thread
            wakeDependencies(root, owner);

            // execute the root "thread" in the caller's thread
            root.setStatus(Status.READY);
            eval(root);

            handleErrors(root);
            cleanup(root);

            log.info("resume ['{}'] -> done", eventRef);
            return root;
        });
    }

    private void eval(State state) {
        Stack<Command> stack = state.getStack();

        try {
            while (true) {
                if (state.getStatus() == Status.SUSPENDED) {
                    break;
                }

                if (stack.isEmpty()) {
                    state.setStatus(Status.DONE);
                    break;
                }

                Command cmd = stack.peek();
                cmd.eval(ctx, state);
            }
        } catch (Throwable t) {
            state.setStatus(Status.DONE);
            state.setLastError(t);
        }
    }

    private void spawn(State state) {
        executor.submit(() -> eval(state));
    }

    /**
     * Resumes all "threads" of the target state's tree.
     */
    private void wakeDependencies(State root, State target) {
        target.setStatus(Status.READY);
        spawn(target);

        State parent = findParent(root, target);
        if (parent == root || parent == null) {
            return;
        }

        wakeDependencies(root, parent);
    }

    private synchronized <T> T once(Callable<T> r) throws Exception {
        assertOpen();

        try {
            return r.call();
        } finally {
            executor.shutdown();
            closed = true;
        }
    }

    private void assertOpen() {
        if (closed) {
            throw new IllegalStateException("Runtime objects shouldn't be re-used");
        }
    }

    /**
     * Removes completed "threads" from the state.
     */
    private static void cleanup(State state) {
        if (state.getStatus() != Status.DONE) {
            return;
        }

        for (Iterator<State> i = state.getChildren().iterator(); i.hasNext(); ) {
            State c = i.next();

            if (c.getStatus() == Status.DONE) {
                i.remove();
            }

            cleanup(c);
        }
    }

    /**
     * Returns a parent state object of the specified child.
     */
    private static State findParent(State root, State child) {
        if (root.getChildren().contains(child)) {
            return root;
        }

        for (State c : root.getChildren()) {
            State s = findParent(c, child);
            if (s != null) {
                return s;
            }
        }

        return null;
    }

    /**
     * Returns a state object which owns the specified {@code eventRef}.
     */
    private static State findOwner(State root, String eventRef) {
        if (root.getEventRefs().contains(eventRef)) {
            return root;
        }

        for (State c : root.getChildren()) {
            State t = findOwner(c, eventRef);
            if (t != null) {
                return t;
            }
        }

        return null;
    }

    /**
     * Rethrows errors stored in the state.
     */
    private static void handleErrors(State state) {
        Throwable t = state.getLastError();
        if (t == null) {
            return;
        }

        // avoid unnecessary wrapping
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }

        throw new RuntimeException(t);
    }

    private class RuntimeContextImpl implements RuntimeContext {

        private final AtomicLong stateIdSeq = new AtomicLong(System.currentTimeMillis());

        @Override
        public StateId nextStateId() {
            return new StateId(stateIdSeq.getAndIncrement());
        }

        @Override
        public void spawn(State state) {
            Runtime.this.spawn(state);
        }
    }
}
