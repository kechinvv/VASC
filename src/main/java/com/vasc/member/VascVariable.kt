package com.vasc.member

import com.vasc.type.VascType

data class VascVariable(
    val name: String,
    val type: VascType,
    var isInitialized: Boolean
) {
    override fun toString() = "$name: $type"
}