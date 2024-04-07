package com.vasc.member

import com.vasc.type.VascType

sealed class VascParametrized(
    val parameters: List<VascVariable>
) {
    val parameterTypes: List<VascType> = parameters.map { it.type }

    override fun toString() = parameters.joinToString(", ", "(", ")")
}