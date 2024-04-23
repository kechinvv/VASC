package com.vasc.member

import com.vasc.type.VascType
import com.vasc.type.VascVoid

open class VascMethod(
    val name: String,
    parameters: List<VascVariable>,
    val returnType: VascType
) : VascParametrized(parameters) {

    override fun toString() = "$name${super.toString()}${returnType.let { if (it is VascVoid) "" else ": $it" }}"
}