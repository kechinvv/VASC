package com.vasc.member

import com.vasc.type.VascType

data class VascMethod(
    val name: String,
    val parameters: List<VascVariable>,
    val returnType: VascType
){
    override fun toString() = "$name${parameters.joinToString(", ", "(", ")")}"
}