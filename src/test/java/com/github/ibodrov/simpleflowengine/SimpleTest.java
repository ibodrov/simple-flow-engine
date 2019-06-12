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

import com.github.ibodrov.simpleflowengine.commands.Block;
import com.github.ibodrov.simpleflowengine.commands.Command;
import com.github.ibodrov.simpleflowengine.commands.Suspend;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.github.ibodrov.simpleflowengine.commands.Block.Strategy.PARALLEL;
import static java.util.Arrays.asList;

public class SimpleTest {

    @Test
    public void test() throws Exception {
        Command program = new Block(asList(
                tag("hello", new Debug("hello!")),
                new Block(PARALLEL, asList(
                        new Block(asList(
                                tag("beforeA", new Debug("before suspend A")),
                                new TestSuspend("a"),
                                new Block(PARALLEL, asList(
                                        new Sleep(2000),
                                        new Sleep(1000)
                                )),
                                tag("afterA", new Debug("after suspend A"))
                        )),
                        new Block(asList(
                                new Debug("before suspend B"),
                                new Block(PARALLEL, asList(
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
                tag("goodbye", new Debug("goodbye!"))
        ));

        RecordingListener listener = new RecordingListener();

        State state = start(listener, program);
        assertTags(listener, "hello", "beforeA");
        state = serializationRoundtrip(state);

        state = resume(listener, state, "c");
        state = serializationRoundtrip(state);

        state = resume(listener, state, "b");
        state = serializationRoundtrip(state);

        state = resume(listener, state, "a");
        assertTags(listener, "goodbye");
        state = serializationRoundtrip(state);
    }

    private static State start(RuntimeListener listener, Command program) throws Exception {
        return new Runtime.Builder()
                .withListener(listener)
                .build()
                .start(program);
    }

    private static State resume(RuntimeListener listener, State state, String eventRef) throws Exception {
        return new Runtime.Builder()
                .withListener(listener)
                .build()
                .resume(state, eventRef);
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

    private static Command tag(String tag, Command cmd) {
        return new Tagged(tag, cmd);
    }

    private static void assertTags(RecordingListener listener, String ... tags) {
        List<Command> l = listener.purge();

        Iterator<Command> i = l.iterator();
        for (String t : tags) {
            if (!i.hasNext()) {
                throw new IllegalStateException("Tag not found: " + t);
            }

            while (true) {
                Command c = i.next();
                if (c instanceof Tagged) {
                    if (t.equals(((Tagged) c).getTag())) {
                        break;
                    }
                }
            }
        }
    }

    public static class TestException implements Command {

        private static final long serialVersionUID = 1L;

        @Override
        public void eval(RuntimeContext ctx, State state) {
            throw new RuntimeException("Whoops!");
        }
    }

    public static class TestSuspend implements Command {

        private static final long serialVersionUID = 1L;

        private final String eventRef;

        public TestSuspend(String eventRef) {
            this.eventRef = eventRef;
        }

        @Override
        public void eval(RuntimeContext ctx, State state) {
            Stack<Command> stack = state.getStack();
            stack.pop();

            stack.push(new Suspend(eventRef));
        }
    }

    public static class Debug implements Command {

        private static final long serialVersionUID = 1L;
        private static final Logger log = LoggerFactory.getLogger(Debug.class);

        private final String message;

        public Debug(String message) {
            this.message = message;
        }

        @Override
        public void eval(RuntimeContext ctx, State state) {
            Stack<Command> stack = state.getStack();
            stack.pop();

            log.info("Debug -> {}", message);
        }
    }

    public static class Sleep implements Command {

        private static final long serialVersionUID = 1L;
        private static final Logger log = LoggerFactory.getLogger(Sleep.class);

        private final long ms;

        public Sleep(long ms) {
            this.ms = ms;
        }

        @Override
        public void eval(RuntimeContext ctx, State state) {
            Stack<Command> stack = state.getStack();
            stack.pop();

            log.info("Sleep -> {}ms...", ms);
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static class Tagged implements Command {

        private final String tag;
        private final Command delegate;

        public Tagged(String tag, Command delegate) {
            this.tag = tag;
            this.delegate = delegate;
        }

        @Override
        public void eval(RuntimeContext ctx, State state) {
            delegate.eval(ctx, state);
        }

        public String getTag() {
            return tag;
        }
    }

    public static class RecordingListener implements RuntimeListener {

        private final List<Command> commands = new ArrayList<>();

        public List<Command> purge() {
            synchronized (commands) {
                List<Command> l = new ArrayList<>(commands);
                commands.clear();
                return l;
            }
        }

        @Override
        public void beforeCommand(Command cmd) {
        }

        @Override
        public void afterCommand(Command cmd) {
            synchronized (commands) {
                commands.add(cmd);
            }
        }
    }
}
