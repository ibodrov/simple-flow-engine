package com.github.ibodrov.simpleflowengine;

import java.io.Serializable;
import java.util.Objects;

public class StateId implements Serializable {

    private final long id;

    public StateId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateId threadId = (StateId) o;
        return id == threadId.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StateId{" +
                "id=" + id +
                '}';
    }
}
