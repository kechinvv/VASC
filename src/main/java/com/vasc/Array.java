package com.vasc;

import java.util.Arrays;

public class Array<T> {
    private final T[] value;

    public Array(int length) {
        this.value = (T[]) new Object[length];
    }

    public com.vasc.List toList() {
        return new List<>(value);
    }

    public com.vasc.Integer length() {
        return new Integer(value.length);
    }

    public T get(com.vasc.Integer index) {
        return value[(int) index.value];
    }

    public void set(com.vasc.Integer index, T v) {
        value[(int) index.value] = v;
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }
}
