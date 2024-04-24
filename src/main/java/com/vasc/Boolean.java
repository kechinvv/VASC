package com.vasc;

public class Boolean {
    public final boolean value;

    public Boolean(boolean value) {
        this.value = value;
    }

    public com.vasc.Integer toInteger() {
        return new Integer(value ? 1 : 0);
    }

    public com.vasc.Boolean or(com.vasc.Boolean other) {
        return new Boolean(this.value || other.value);
    }

    public com.vasc.Boolean and(com.vasc.Boolean other) {
        return new Boolean(this.value && other.value);
    }

    public com.vasc.Boolean xor(com.vasc.Boolean other) {
        return new Boolean(this.value ^ other.value);
    }

    public com.vasc.Boolean not() {
        return new Boolean(!this.value);
    }

    @Override
    public String toString() {
        return java.lang.Boolean.toString(value);
    }
}
