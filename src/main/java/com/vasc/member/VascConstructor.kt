package com.vasc.member

data class VascConstructor(
    val parameters: List<VascVariable>
) {
    companion object {
        fun empty() = VascConstructor(emptyList())
    }

    override fun toString() = "this${parameters.joinToString(", ", "(", ")")}"
}