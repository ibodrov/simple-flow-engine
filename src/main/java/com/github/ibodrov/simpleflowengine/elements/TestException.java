package com.github.ibodrov.simpleflowengine.elements;

import com.github.ibodrov.simpleflowengine.RuntimeContext;
import com.github.ibodrov.simpleflowengine.State;

public class TestException implements Element {

    @Override
    public void eval(RuntimeContext ctx, State state) {
        throw new RuntimeException("Whoops!");
    }
}
