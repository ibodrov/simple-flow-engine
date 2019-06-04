package com.github.ibodrov.simpleflowengine.elements;

import com.github.ibodrov.simpleflowengine.RuntimeContext;
import com.github.ibodrov.simpleflowengine.State;

import java.io.Serializable;

public interface Element extends Serializable {

    void eval(RuntimeContext ctx, State state);
}
