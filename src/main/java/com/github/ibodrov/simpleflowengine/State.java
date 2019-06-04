package com.github.ibodrov.simpleflowengine;

import com.github.ibodrov.simpleflowengine.commands.Command;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class State implements Serializable {

    private static final long serialVersionUID = 1L;

    private final StateId id;
    private final Stack<Command> stack = new Stack<>();
    private final Set<String> eventRefs = new HashSet<>();
    private final Set<State> children = new HashSet<>();

    private Status status = Status.READY;

    public State(StateId id) {
        this.id = id;
    }

    public StateId getId() {
        return id;
    }

    public Stack<Command> getStack() {
        return stack;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<State> getChildren() {
        return children;
    }

    public Set<String> getEventRefs() {
        return eventRefs;
    }

    public enum Status {
        READY,
        COMPLETE,
        SUSPENDED
    }
}
