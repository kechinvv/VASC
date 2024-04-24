package com.vasc.member

import com.vasc.type.VascType
import com.vasc.type.VascVoid

open class VascMethod(
    val name: String,
    parameters: List<VascVariable>,
    val returnType: VascType
) : VascParametrized(parameters) {

    override fun toString() = "$name${super.toString()}${returnType.let { if (it is VascVoid) "" else ": $it" }}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VascMethod) return false

        return name == other.name && parameterTypes == other.parameterTypes
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + parameterTypes.hashCode()
        return result
    }
}