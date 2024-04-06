package com.vasc.type

import com.vasc.member.*

open class VascClass(override val name: String) : VascType {

    override val parent: VascType? = null

    override val declaredFields: List<VascVariable> = emptyList()
    override val declaredConstructors: List<VascConstructor> = emptyList()
    override val declaredMethods: List<VascMethod> = emptyList()

    override val fields: List<VascVariable>
        get() = declaredFields + (parent?.fields ?: emptyList())

    override val methods: List<VascMethod>
        get() = declaredMethods + (parent?.methods ?: emptyList())

    override fun getDeclaredField(name: String): VascVariable? {
        return fields.find { it.name == name }
    }

    override fun getDeclaredConstructor(parameterTypes: List<VascType>): VascConstructor? {
        return declaredConstructors.find { it.parameterTypes == parameterTypes }
    }

    override fun getDeclaredMethod(name: String, parameterTypes: List<VascType>): VascMethod? {
        return methods.find { it.name == name && it.parameterTypes == parameterTypes }
    }

    override fun getField(name: String): VascVariable? {
        return getDeclaredField(name) ?: parent?.getField(name)
    }

    override fun getMethod(name: String, parameterTypes: List<VascType>): VascMethod? {
        return getDeclaredMethod(name, parameterTypes) ?: parent?.getMethod(name, parameterTypes)
    }

    override fun toString() = name
}