package com.vasc.typecheck

import com.vasc.type.VascType

class Scope(
    private val vars: MutableMap<String, VascType>,
    private val parent: Scope? = null,
    private val returnT: VascType? = null,
    private val classT: VascType? = null
) {

    fun find(name: String): VascType? {
        return vars[name] ?: parent?.find(name)
    }

    fun add(name: String, t: VascType) {
        vars[name] = t
    }

    fun enclosed(
        vars: MutableMap<String, VascType> = mutableMapOf(),
        returnT: VascType? = null,
        classT: VascType? = null
    )
        = Scope(vars, parent, returnT, classT)

    fun returnT(): VascType? {
        return returnT ?: parent?.returnT()
    }

    fun classT(): VascType? {
        return classT ?: parent?.classT()
    }
}
