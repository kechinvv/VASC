package com.vasc;

import java.util.ArrayList;

public class Array<T> {
    private final ArrayList<T> value;

    public Array(int length) {
        this.value = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            value.add(null);
        }
    }

    public com.vasc.List toList() {
        return new List<>(value);
    }

    public com.vasc.Integer length() {
        return new Integer(value.size());
    }

    public T get(com.vasc.Integer index) {
        return value.get((int) index.value);
    }

    public void set(com.vasc.Integer index, T v) {
        value.set((int) index.value, v);
    }
}
