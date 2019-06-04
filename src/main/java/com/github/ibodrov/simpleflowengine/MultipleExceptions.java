package com.github.ibodrov.simpleflowengine;

import java.util.Iterator;
import java.util.List;

public class MultipleExceptions extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<Throwable> causes;

    public MultipleExceptions(List<Throwable> causes) {
        super(formatMessage(causes));
        this.causes = causes;
    }

    public List<Throwable> getCauses() {
        return causes;
    }

    private static String formatMessage(List<Throwable> causes) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Throwable> i = causes.iterator(); i.hasNext();) {
            Throwable t = i.next();
            sb.append("[").append(t.getMessage()).append("]");
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
