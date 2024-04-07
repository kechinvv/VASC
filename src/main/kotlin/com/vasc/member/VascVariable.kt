package com.vasc.member

import com.vasc.type.VascType

class VascVariable(
    val name: String,
    val type: VascType,
    var isInitialized: Boolean
) {

    companion object {
        infix fun String.colon(type: VascType): VascVariable = VascVariable(this, type, true)
    }

    override fun toString() = "$name: $type"
}