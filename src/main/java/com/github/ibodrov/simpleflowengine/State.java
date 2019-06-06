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

import com.github.ibodrov.simpleflowengine.commands.Command;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates all state of the thread (process) and its children.
 * Can be serialized and used to restore the process state later.
 */
public class State implements Serializable {

    private static final long serialVersionUID = 1L;

    private final StateId id;
    private final Stack<Command> stack = new Stack<>();
    private final Set<String> eventRefs = new HashSet<>();
    private final Set<State> children = new HashSet<>();

    private Status status = Status.READY;
    private Throwable lastError;

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

    public Throwable getLastError() {
        return lastError;
    }

    public void setLastError(Throwable lastError) {
        this.lastError = lastError;
    }

    public enum Status {
        READY,
        SUSPENDED,
        DONE
    }
}
