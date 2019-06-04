package com.github.ibodrov.simpleflowengine.commands;

import com.github.ibodrov.simpleflowengine.elements.Element;

public class EvalElement implements Command {

    private static final long serialVersionUID = 1L;

    private final Element element;

    public EvalElement(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
