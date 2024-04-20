package com.vasc;

public class Real {
    public final double value;

    public Real(double value) {
        this.value = value;
    }

    public Real(com.vasc.Real value) {
        this.value = value.value;
    }

    public Real(com.vasc.Integer value) {
        this.value = value.value;
    }


    public com.vasc.Integer toInteger() {
        return new Integer((long) value);
    }

    public com.vasc.Real unaryMinus() {
        return new Real(-value);
    }

    public com.vasc.Real sqrt() {
        return new Real(Math.sqrt(value));
    }


    public com.vasc.Real plus(com.vasc.Real other) {
        return new Real(value + other.value);
    }

    public com.vasc.Real plus(com.vasc.Integer other) {
        return new Real(value + other.value);
    }

    public com.vasc.Real minus(com.vasc.Real other) {
        return new Real(value - other.value);
    }

    public com.vasc.Real minus(com.vasc.Integer other) {
        return new Real(value - other.value);
    }

    public com.vasc.Real mul(com.vasc.Real other) {
        return new Real(value * other.value);
    }

    public com.vasc.Real mul(com.vasc.Integer other) {
        return new Real(value * other.value);
    }

    public com.vasc.Real div(com.vasc.Real other) {
        return new Real(value / other.value);
    }

    public com.vasc.Real div(com.vasc.Integer other) {
        return new Real(value / other.value);
    }

    public com.vasc.Real rem(com.vasc.Integer other) {
        return new Real(value % other.value);
    }


    public com.vasc.Boolean less(com.vasc.Real other) {
        return new Boolean(value < other.value);
    }

    public com.vasc.Boolean less(com.vasc.Integer other) {
        return new Boolean(value < other.value);
    }

    public com.vasc.Boolean lessEqual(com.vasc.Real other) {
        return new Boolean(value <= other.value);
    }

    public com.vasc.Boolean lessEqual(com.vasc.Integer other) {
        return new Boolean(value <= other.value);
    }

    public com.vasc.Boolean greater(com.vasc.Real other) {
        return new Boolean(value > other.value);
    }

    public com.vasc.Boolean greater(com.vasc.Integer other) {
        return new Boolean(value > other.value);
    }

    public com.vasc.Boolean greaterEqual(com.vasc.Real other) {
        return new Boolean(value >= other.value);
    }

    public com.vasc.Boolean greaterEqual(com.vasc.Integer other) {
        return new Boolean(value >= other.value);
    }

    public com.vasc.Boolean equal(com.vasc.Real other) {
        return new Boolean(value == other.value);
    }

    public com.vasc.Boolean equal(com.vasc.Integer other) {
        return new Boolean(value == other.value);
    }
}
