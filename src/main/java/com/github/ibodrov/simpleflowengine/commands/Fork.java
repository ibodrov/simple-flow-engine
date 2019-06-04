package com.github.ibodrov.simpleflowengine.commands;

import com.github.ibodrov.simpleflowengine.StateId;
import com.github.ibodrov.simpleflowengine.elements.Element;

public class Fork implements Command {

    private static final long serialVersionUID = 1L;

    private final StateId id;
    private final Element element;

    public Fork(StateId id, Element element) {
        this.id = id;
        this.element = element;
    }

    public StateId getId() {
        return id;
    }

    public Element getElement() {
        return element;
    }
}
