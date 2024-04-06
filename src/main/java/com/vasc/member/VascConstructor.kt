package com.vasc.member

class VascConstructor(
    parameters: List<VascVariable>
) : VascParametrized(parameters) {

    override fun toString() = "this${super.toString()}"
}