package com.vasc.member

import com.vasc.type.VascType

class VascVariable(
    val name: String,
    val type: VascType
) {

    companion object {
        infix fun String.colon(type: VascType): VascVariable = VascVariable(this, type)
    }

    override fun toString() = "$name: $type"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VascVariable) return false

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}