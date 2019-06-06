package com.github.ibodrov.simpleflowengine;

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

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Simple stack interface.
 */
public class Stack<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Deque<T> items = new LinkedList<>();

    public void push(T item) {
        items.push(item);
    }

    public T peek() {
        return items.peek();
    }

    public T pop() {
        return items.pop();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
