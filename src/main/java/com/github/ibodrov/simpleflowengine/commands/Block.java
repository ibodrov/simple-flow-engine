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

import com.github.ibodrov.simpleflowengine.RuntimeContext;
import com.github.ibodrov.simpleflowengine.Stack;
import com.github.ibodrov.simpleflowengine.State;
import com.github.ibodrov.simpleflowengine.StateId;

import java.util.*;
import java.util.stream.Collectors;

public class Block implements Command {

    private static final long serialVersionUID = 1L;

    private final String strategyRef;
    private final List<Command> commands;

    public Block(List<Command> commands) {
        this("sequential", commands);
    }

    public Block(String strategyRef, List<Command> commands) {
        this.strategyRef = strategyRef;
        this.commands = commands;
    }

    @Override
    public void eval(RuntimeContext ctx, State state) {
        Stack<Command> stack = state.getStack();
        stack.pop();

        if ("sequential".equals(strategyRef)) {
            // sequential execution is very simple: we just need to add
            // each command of the block onto the stack

            List<Command> l = new ArrayList<>(commands);

            // to preserve the original order the commands must be added onto
            // the stack in the reversed order
            Collections.reverse(l);

            l.forEach(stack::push);
        } else if ("parallel".equals(strategyRef)) {
            // parallel execution consist of creating "forks" for each command
            // and a combined "join"

            List<Map.Entry<StateId, Command>> forks = commands.stream()
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
