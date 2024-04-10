package com.vasc.typecheck

import com.vasc.type.VascType

class Scope(
    private val vars: MutableMap<String, VascType>,
    private val parent: Scope? = null,
) {

    fun find(name: String): VascType? {
        return vars[name] ?: parent?.find(name)
    }

    fun add(name: String, t: VascType) {
        vars[name] = t
    }

    fun enclosed(
        vars: MutableMap<String, VascType> = mutableMapOf()
    )
        = Scope(vars, this)
}
