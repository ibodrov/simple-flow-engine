package com.github.ibodrov.simpleflowengine;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

public class Stack<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Deque<T> items = new LinkedList<>();

    public void push(T item) {
        items.push(item);
    }

    public T peek() {
        return items.peek();
    }

    public T pop() {
        return items.pop();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
