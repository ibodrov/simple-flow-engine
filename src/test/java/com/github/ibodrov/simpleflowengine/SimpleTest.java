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

import com.github.ibodrov.simpleflowengine.elements.*;
import org.junit.Test;

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

        program = new Block(asList(
                new Debug("hello!"),
                new Block("parallel", asList(
                        new TestException(),
                        new TestException(),
                        new Debug("!")
                )),
                new Debug("goodbye!")
        ));

        State state = new Runtime().start(program);
//        state = new Runtime().resume(state, "c");
//        state = new Runtime().resume(state, "b");
//        state = new Runtime().resume(state, "a");
    }
}
