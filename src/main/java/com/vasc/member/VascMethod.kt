package com.vasc.member

import com.vasc.type.VascType

class VascMethod(
    val name: String,
    parameters: List<VascVariable>,
    val returnType: VascType?
) : VascParametrized(parameters) {

    override fun toString() = "$name${super.toString()}${returnType?.let { ": $it" } ?: ""}"
}