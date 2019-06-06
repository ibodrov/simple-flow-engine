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
import com.github.ibodrov.simpleflowengine.commands.Suspend;
import com.github.ibodrov.simpleflowengine.elements.Block;
import com.github.ibodrov.simpleflowengine.elements.Element;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static java.util.Arrays.asList;

public class SimpleTest {

    @Test
    public void test() throws Exception {
        Element program = new Block(asList(
                new Debug("hello!"),
                new Block("parallel", asList(
                        new Block(asList(
                                new Debug("before suspend A"),
                                new TestSuspend("a"),
                                new Block("parallel", asList(
                                        new Sleep(2000),
                                        new Sleep(1000)
                                )),
                                new Debug("after suspend A")
                        )),
                        new Block(asList(
                                new Debug("before suspend B"),
                                new Block("parallel", asList(
                                        new Sleep(500),
                                        new Block(asList(
                                                new Debug("before suspend C"),
                                                new TestSuspend("c"),
                                                new Debug("after suspend C")
                                        )),
                                        new Sleep(750)
                                )),
                                new TestSuspend("b"),
                                new Debug("after suspend B")
                        ))
                )),
                new Debug("goodbye!")
        ));

        State state = new Runtime().start(program);
        state = serializationRoundtrip(state);

        state = new Runtime().resume(state, "c");
        state = serializationRoundtrip(state);

        state = new Runtime().resume(state, "b");
        state = serializationRoundtrip(state);

        state = new Runtime().resume(state, "a");
        state = serializationRoundtrip(state);
    }

    private static State serializationRoundtrip(State state) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(baos)) {
            out.writeObject(state);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream in = new ObjectInputStream(bais)) {
            return (State) in.readObject();
        }
    }

    public static class TestException implements Element {

        private static final long serialVersionUID = 1L;

        @Override
        public void eval(RuntimeContext ctx, State state) {
            throw new RuntimeException("Whoops!");
        }
    }

    public static class TestSuspend implements Element {

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

    public static class Debug implements Element {

        private static final long serialVersionUID = 1L;
        private static final Logger log = LoggerFactory.getLogger(Debug.class);

        private final String message;

        public Debug(String message) {
            this.message = message;
        }

        @Override
        public void eval(RuntimeContext ctx, State state) {
            log.info("Debug -> {}", message);
        }
    }

    public static class Sleep implements Element {

        private static final long serialVersionUID = 1L;
        private static final Logger log = LoggerFactory.getLogger(Sleep.class);

        private final long ms;

        public Sleep(long ms) {
            this.ms = ms;
        }

        @Override
        public void eval(RuntimeContext ctx, State state) {
            log.info("Sleep -> {}ms...", ms);
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
