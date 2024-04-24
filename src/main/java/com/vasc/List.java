package com.vasc;

import java.util.Collection;
import java.util.LinkedList;

public class List<T> {
    private final LinkedList<T> value;

    public List() {
        value = new LinkedList<>();
    }

    public List(Collection<T> c) {
        this();
        value.addAll(c);
    }

    public List(T elem) {
        value = new LinkedList<>();
        value.add(elem);
    }

    public List(T elem, com.vasc.Integer count) {
        value = new LinkedList<>();
        for (int i = 0; i < count.value; i++) {
            value.addLast(elem);
        }
    }


    public com.vasc.List<T> append(T elem) {
        value.addLast(elem);
        return this;
    }

    public T head() {
        return value.getFirst();
    }

    public com.vasc.List<T> tail() {
        var ret = new List<T>();
        ret.value.addAll(value.subList(1, value.size()));
        return ret;
    }

    public com.vasc.Boolean isEmpty() {
        return new Boolean(value.isEmpty());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
