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
        return declaredFields.find { it.name == name }
    }

    override fun getDeclaredConstructor(parameterTypes: List<VascType>): VascConstructor? {
        return declaredConstructors.find { it.isApplicableTo(parameterTypes) }
    }

    override fun getDeclaredMethod(name: String, parameterTypes: List<VascType>): VascMethod? {
        return declaredMethods.find { it.name == name && it.isApplicableTo(parameterTypes) }
    }

    override fun getField(name: String): VascVariable? {
        return getDeclaredField(name) ?: parent?.getField(name)
    }

    override fun getMethod(name: String, parameterTypes: List<VascType>): VascMethod? {
        return getDeclaredMethod(name, parameterTypes) ?: parent?.getMethod(name, parameterTypes)
    }

    override fun isAssignableFrom(subtype: VascType): Boolean {
        var current: VascType? = subtype
        while (current != null) {
            if (this.name == current.name) return true
            current = current.parent
        }
        return false
    }

    override fun toString() = name
}