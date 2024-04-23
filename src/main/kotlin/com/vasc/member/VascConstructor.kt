package com.vasc.member

class VascConstructor(
    parameters: List<VascVariable>
) : VascParametrized(parameters) {

    companion object {
        fun `this`(vararg parameters: VascVariable) = VascConstructor(parameters.toList())
    }

    override fun toString() = "this${super.toString()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VascConstructor) return false

        return parameterTypes == other.parameterTypes
    }

    override fun hashCode(): Int {
        return parameterTypes.hashCode()
    }
}