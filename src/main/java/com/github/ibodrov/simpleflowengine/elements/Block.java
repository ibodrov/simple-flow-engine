package com.github.ibodrov.simpleflowengine.elements;

import com.github.ibodrov.simpleflowengine.RuntimeContext;
import com.github.ibodrov.simpleflowengine.Stack;
import com.github.ibodrov.simpleflowengine.State;
import com.github.ibodrov.simpleflowengine.StateId;
import com.github.ibodrov.simpleflowengine.commands.Command;
import com.github.ibodrov.simpleflowengine.commands.EvalElement;
import com.github.ibodrov.simpleflowengine.commands.Fork;
import com.github.ibodrov.simpleflowengine.commands.Join;

import java.util.*;
import java.util.stream.Collectors;

public class Block implements Element {

    private static final long serialVersionUID = 1L;

    private final String strategyRef;
    private final List<Element> elements;

    public Block(List<Element> elements) {
        this("sequential", elements);
    }

    public Block(String strategyRef, List<Element> elements) {
        this.strategyRef = strategyRef;
        this.elements = elements;
    }

    @Override
    public void eval(RuntimeContext ctx, State state) {
        Stack<Command> stack = state.getStack();

        if ("sequential".equals(strategyRef)) {
            // reverse the order of insertion to preserver the original order of definition
            List<Element> l = new ArrayList<>(elements);
            Collections.reverse(l);
            l.forEach(e -> stack.push(new EvalElement(e)));
        } else if ("parallel".equals(strategyRef)) {
            List<Map.Entry<StateId, Element>> forks = elements.stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(ctx.nextStateId(), e))
                    .collect(Collectors.toList());

            stack.push(new Join(forks.stream().map(Map.Entry::getKey).collect(Collectors.toSet())));

            forks.forEach(f -> stack.push(new Fork(f.getKey(), f.getValue())));
        } else {
            throw new IllegalStateException("Unknown block strategyRef: " + strategyRef);
        }
    }
}
