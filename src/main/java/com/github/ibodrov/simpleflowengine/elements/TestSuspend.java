package com.github.ibodrov.simpleflowengine.elements;

import com.github.ibodrov.simpleflowengine.RuntimeContext;
import com.github.ibodrov.simpleflowengine.Stack;
import com.github.ibodrov.simpleflowengine.State;
import com.github.ibodrov.simpleflowengine.commands.Command;
import com.github.ibodrov.simpleflowengine.commands.Suspend;

public class TestSuspend implements Element {

    private static final long serialVersionUID = 1L;

    private final String eventRef;

    public TestSuspend(String eventRef) {
        this.eventRef = eventRef;
    }

    @Override
    public void eval(RuntimeContext ctx, State state) {
        Stack<Command> stack = state.getStack();
        stack.push(new Suspend(eventRef));
    }
}
