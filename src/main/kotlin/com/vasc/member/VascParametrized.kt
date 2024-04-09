package com.vasc.member

import com.vasc.type.VascType

sealed class VascParametrized(
    val parameters: List<VascVariable>
) {
    val parameterTypes: List<VascType> = parameters.map { it.type }

    fun isApplicableTo(types: List<VascType>): Boolean {
        if (parameterTypes.size != types.size) return false
        for (i in parameterTypes.indices) {
            if (!parameterTypes[i].isAssignableFrom(types[i])) return false
        }
        return true
    }

    override fun toString() = parameters.joinToString(", ", "(", ")")
}