package com.github.ibodrov.simpleflowengine.commands;

import com.github.ibodrov.simpleflowengine.StateId;

import java.util.Set;

public class Join implements Command {

    private static final long serialVersionUID = 1L;

    private final Set<StateId> ids;

    public Join(Set<StateId> ids) {
        this.ids = ids;
    }

    public Set<StateId> getIds() {
        return ids;
    }
}
