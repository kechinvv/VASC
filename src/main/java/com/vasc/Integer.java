package com.vasc;

public class Integer {
    public final long value;

    public Integer(long value) {
        this.value = value;
    }

    public Integer(com.vasc.Integer value) {
        this.value = value.value;
    }

    public Integer(com.vasc.Real value) {
        this.value = (long) value.value;
    }


    public com.vasc.Real toReal() {
        return new Real(this);
    }

    public com.vasc.Boolean toBoolean() {
        return new Boolean(value != 0);
    }

    public com.vasc.Integer unaryMinus() {
        return new Integer(-value);
    }


    public com.vasc.Integer plus(com.vasc.Integer other) {
        return new Integer(value + other.value);
    }

    public com.vasc.Integer plus(com.vasc.Real other) {
        return new Integer(value + (long) other.value);
    }

    public com.vasc.Integer minus(com.vasc.Integer other) {
        return new Integer(value - other.value);
    }

    public com.vasc.Integer minus(com.vasc.Real other) {
        return new Integer(value - (long) other.value);
    }

    public com.vasc.Integer mul(com.vasc.Integer other) {
        return new Integer(value * other.value);
    }

    public com.vasc.Integer mul(com.vasc.Real other) {
        return new Integer(value * (long) other.value);
    }

    public com.vasc.Integer div(com.vasc.Integer other) {
        return new Integer(value * other.value);
    }

    public com.vasc.Integer div(com.vasc.Real other) {
        return new Integer(value / (long) other.value);
    }

    public com.vasc.Integer rem(com.vasc.Integer other) {
        return new Integer(value % other.value);
    }


    public com.vasc.Boolean less(com.vasc.Integer other) {
        return new Boolean(value < other.value);
    }

    public com.vasc.Boolean less(com.vasc.Real other) {
        return new Boolean(value < other.value);
    }

    public com.vasc.Boolean lessEqual(com.vasc.Integer other) {
        return new Boolean(value <= other.value);
    }

    public com.vasc.Boolean lessEqual(com.vasc.Real other) {
        return new Boolean(value <= other.value);
    }

    public com.vasc.Boolean greater(com.vasc.Integer other) {
        return new Boolean(value > other.value);
    }

    public com.vasc.Boolean greater(com.vasc.Real other) {
        return new Boolean(value > other.value);
    }

    public com.vasc.Boolean greaterEqual(com.vasc.Integer other) {
        return new Boolean(value >= other.value);
    }

    public com.vasc.Boolean greaterEqual(com.vasc.Real other) {
        return new Boolean(value >= other.value);
    }

    public com.vasc.Boolean equal(com.vasc.Integer other) {
        return new Boolean(value == other.value);
    }

    public com.vasc.Boolean equal(com.vasc.Real other) {
        return new Boolean(value == other.value);
    }

    @Override
    public String toString() {
        return java.lang.Long.toString(value);
    }
}
