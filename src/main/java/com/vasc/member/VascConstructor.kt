package com.vasc.member

class VascConstructor(
    parameters: List<VascVariable>
) : VascParametrized(parameters) {

    companion object {
        fun `this`(vararg parameters: VascVariable) = VascConstructor(parameters.toList())
    }

    override fun toString() = "this${super.toString()}"
}