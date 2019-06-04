package com.github.ibodrov.simpleflowengine;

import com.github.ibodrov.simpleflowengine.elements.Block;
import com.github.ibodrov.simpleflowengine.elements.Debug;
import com.github.ibodrov.simpleflowengine.elements.Element;
import com.github.ibodrov.simpleflowengine.elements.TestSuspend;
import org.junit.Test;

import java.util.Arrays;

public class SimpleTest {

    @Test
    public void test() throws Exception {
        Element program = new Block(Arrays.asList(
                new Debug("hello!"),
                new Block(Arrays.asList(
                        new Debug("before suspend"),
                        new TestSuspend("abc"),
                        new Block("parallel", Arrays.asList(
                                new Debug("a"),
                                new Debug("b"),
                                new Debug("c")
                        )),
                        new Debug("after suspend"))),
                new Debug("goodbye!")
        ));

        State state = new Runtime().start(program);
        state = new Runtime().resume(state, "abc");
    }
}
