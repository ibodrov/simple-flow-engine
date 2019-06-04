package com.github.ibodrov.simpleflowengine.commands;

public class Suspend implements Command {

    private static final long serialVersionUID = 1L;

    private final String eventRef;

    public Suspend(String eventRef) {
        this.eventRef = eventRef;
    }

    public String getEventRef() {
        return eventRef;
    }
}
