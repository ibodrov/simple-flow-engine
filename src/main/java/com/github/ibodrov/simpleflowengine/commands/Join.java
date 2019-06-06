package com.github.ibodrov.simpleflowengine.commands;

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

import com.github.ibodrov.simpleflowengine.*;
import com.github.ibodrov.simpleflowengine.State.Status;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Waits for the specified child "threads" to complete or suspend.
 * Suspends the current "thread" if there are any suspended children.
 */
public class Join implements Command {

    private static final long serialVersionUID = 1L;

    private final Set<StateId> ids;

    public Join(Set<StateId> ids) {
        this.ids = ids;
    }

    @Override
    public void eval(RuntimeContext ctx, State state) {
        Stack<Command> stack = state.getStack();

        // Here's a very dumb but working solution to the problem
        // of monitoring the child "threads" state - just a loop
        // with a delay. On each iteration it decides whether
        // the join command can be removed from the stack (and thus
        // continuing the execution) or not.
        // We could've used futures instead, but it's way more
        // complicated - especially when suspend/resume are involved.

        while (true) {
            boolean done = allDone(state, ids);

            // all children are done, proceed with the execution
            if (done) {
                handleErrors(state, ids);
                stack.pop();
                return;
            }

            boolean suspended = any(state, ids, Status.SUSPENDED);
            boolean ready = any(state, ids, Status.READY);

            // all children are either DONE or SUSPENDED - suspend the parent thread
            if (!ready && suspended) {
                handleErrors(state, ids);
                state.setStatus(Status.SUSPENDED);
                return;
            }

            // some children are still running, wait for a bit and then check again
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static State getState(State root, StateId id) {
        return root.getChildren().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("State not found: " + id));
    }

    private static boolean allDone(State root, Set<StateId> ids) {
        return ids.stream().allMatch(id -> getState(root, id).getStatus() == Status.DONE);
    }

    private static boolean any(State root, Set<StateId> ids, Status status) {
        return ids.stream().anyMatch(id -> getState(root, id).getStatus() == status);
    }

    private static List<Throwable> findFailures(State root, Set<StateId> ids) {
        return ids.stream().map(id -> getState(root, id))
                .filter(s -> s.getLastError() != null)
                .map(State::getLastError)
                .collect(Collectors.toList());
    }

    private static void handleErrors(State root, Set<StateId> ids) {
        List<Throwable> failures = findFailures(root, ids);
        if (failures.isEmpty()) {
            return;
        }

        throw new MultipleExceptions(failures);
    }
}
