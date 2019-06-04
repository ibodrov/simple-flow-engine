package com.github.ibodrov.simpleflowengine.elements;

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
            // sequential execution is very simple: we just need to add
            // EvalElements for each element of the block

            List<Element> l = new ArrayList<>(elements);

            // to preserve the original order the elements must be added onto
            // the stack in the reversed order
            Collections.reverse(l);

            l.forEach(e -> stack.push(new EvalElement(e)));
        } else if ("parallel".equals(strategyRef)) {
            // parallel execution consist of creating "forks" for each element
            // and a combined "join"

            List<Map.Entry<StateId, Element>> forks = elements.stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(ctx.nextStateId(), e))
                    .collect(Collectors.toList());

            Collections.reverse(forks);

            stack.push(new Join(forks.stream().map(Map.Entry::getKey).collect(Collectors.toSet())));

            forks.forEach(f -> stack.push(new Fork(f.getKey(), f.getValue())));
        } else {
            throw new IllegalStateException("Unknown block strategyRef: " + strategyRef);
        }
    }
}
