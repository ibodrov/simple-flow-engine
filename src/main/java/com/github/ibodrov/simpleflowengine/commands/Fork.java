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
import com.github.ibodrov.simpleflowengine.elements.Element;

public class Fork implements Command {

    private static final long serialVersionUID = 1L;

    private final StateId id;
    private final Element element;

    public Fork(StateId id, Element element) {
        this.id = id;
        this.element = element;
    }

    @Override
    public void eval(RuntimeContext ctx, State state) {
        Stack<Command> stack = state.getStack();
        stack.pop();

        State child = new State(id);
        child.getStack().push(new EvalElement(element));
        state.getChildren().add(child);

        ctx.spawn(child);
    }
}
