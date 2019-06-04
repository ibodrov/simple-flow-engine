package com.github.ibodrov.simpleflowengine;

import com.github.ibodrov.simpleflowengine.State.Status;
import com.github.ibodrov.simpleflowengine.commands.*;
import com.github.ibodrov.simpleflowengine.elements.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Runtime {

    private static final Logger log = LoggerFactory.getLogger(Runtime.class);

    private final AtomicLong stateIdSeq = new AtomicLong(System.currentTimeMillis()); // TODO better initial value
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<StateId, CompletableFuture<Status>> futures = new HashMap<>();

    public State start(Element e) throws Exception {
        return withResources(() -> {
            State root = new State(nextStateId());

            Stack<Command> stack = root.getStack();
            stack.push(new EvalElement(e));

            eval(root);

            log.info("start -> done");
            return root;
        });
    }

    public State resume(State root, String eventRef) throws Exception {
        log.info("resume ['{}'] -> starting...", eventRef);

        return withResources(() -> {
            State parent = findParent(root, eventRef);
            if (parent != null) {
                parent.setStatus(Status.READY);
            }

            State owner = findOwner(root, eventRef);
            if (owner == null) {
                throw new IllegalStateException("EventRef not found: " + eventRef);
            }

            owner.getEventRefs().remove(eventRef);
            owner.setStatus(Status.READY);

            root.getChildren().forEach(this::wake);

            eval(root);
            log.info("resume ['{}'] -> done", eventRef);

            return root;
        });
    }

    private StateId nextStateId() {
        return new StateId(stateIdSeq.getAndIncrement());
    }

    private void eval(State state) {
        Stack<Command> stack = state.getStack();

        while (true) {
            if (state.getStatus() == Status.SUSPENDED) {
                break;
            }

            if (stack.isEmpty()) {
                state.setStatus(Status.COMPLETE);
                break;
            }

            Command cmd = stack.peek();
            if (cmd instanceof EvalElement) {
                stack.pop();
                eval(state, ((EvalElement) cmd).getElement());
            } else if (cmd instanceof Fork) {
                stack.pop();

                Fork f = (Fork) cmd;

                State child = new State(f.getId());
                child.getStack().push(new EvalElement(f.getElement()));
                state.getChildren().add(child);

                spawn(child);
            } else if (cmd instanceof Join) {
                while (true) {
                    boolean complete;
                    synchronized (futures) {
                        complete = futures.entrySet().stream().allMatch(i -> i.getValue().getNow(Status.READY) == Status.COMPLETE);
                    }
                    if (complete) {
                        stack.pop();
                        break;
                    }

                    boolean suspended;
                    synchronized (futures) {
                        suspended = futures.entrySet().stream().anyMatch(i -> i.getValue().getNow(Status.READY) == Status.SUSPENDED);
                    }
                    if (suspended) {
                        state.setStatus(Status.SUSPENDED);
                        break;
                    }

                    futures.values().stream().filter(i -> i.getNow(Status.READY) == Status.READY)
                            .map(i -> {
                                try {
                                    return i.get();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            } else if (cmd instanceof Suspend) {
                stack.pop();

                state.setStatus(Status.SUSPENDED);
                state.getEventRefs().add(((Suspend) cmd).getEventRef());
            } else {
                throw new IllegalStateException("Unknown command: " + cmd.getClass());
            }
        }
    }

    private void eval(State state, Element element) {
        RuntimeContext ctx = Runtime.this::nextStateId;
        element.eval(ctx, state);
    }

    private void spawn(State state) {
        CompletableFuture<Status> f = new CompletableFuture<>();
        synchronized (futures) {
            futures.put(state.getId(), f);
        }

        executor.submit(() -> {
            try {
                eval(state);
                f.complete(state.getStatus());
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });
    }

    private void wake(State root) {
        root.getChildren().forEach(this::wake);

        if (root.getStatus() != Status.READY) {
            return;
        }

        spawn(root);
    }

    private <T> T withResources(Callable<T> r) throws Exception {
        try {
            return r.call();
        } finally {
            executor.shutdown();
        }
    }

    private static State findParent(State root, String eventRef) {
        if (root.getChildren().stream()
                .anyMatch(i -> i.getEventRefs().contains(eventRef))) {
            return root;
        }

        for (State c : root.getChildren()) {
            State t = findParent(c, eventRef);
            if (t != null) {
                return t;
            }
        }

        return null;
    }

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
}
