package com.vasc.type

import com.vasc.member.*

open class VascClass(override val name: String) : VascType {

    override val parent: VascType? = null
    override val fields: Set<VascVariable> = emptySet()
    override val methods: Set<VascMethod> = emptySet()
    override val constructors: Set<VascConstructor> = setOf(VascConstructor.empty())

    override fun getField(name: String): VascVariable? {
        return fields.find { it.name == name } ?: parent?.getField(name)
    }

    override fun getMethods(name: String): Set<VascMethod> {
        val thisMethods = methods.filter { it.name == name }
        val superMethods = parent?.getMethods(name) ?: emptyList()
        return (thisMethods + superMethods).toSet()
    }

    override fun toString() = name
}