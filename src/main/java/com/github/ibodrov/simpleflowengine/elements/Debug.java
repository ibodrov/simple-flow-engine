package com.github.ibodrov.simpleflowengine.elements;

import com.github.ibodrov.simpleflowengine.RuntimeContext;
import com.github.ibodrov.simpleflowengine.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debug implements Element {

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
