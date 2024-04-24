package com.vasc.type

import com.vasc.member.*

open class VascClass(
    final override val name: String,
    override val parent: VascType? = null
) : VascType {

    override val declaredFields: Collection<VascVariable> = emptySet()
    override val declaredConstructors: Collection<VascConstructor> = emptySet()
    override val declaredMethods: Collection<VascMethod> = emptySet()

    final override val fields: Collection<VascVariable>
        get() = declaredFields.toSet() + (parent?.fields ?: emptySet())

    final override val methods: Collection<VascMethod>
        get() = declaredMethods.toSet() + (parent?.methods ?: emptySet())

    final override fun getDeclaredField(name: String): VascVariable? {
        return declaredFields.find { it.name == name }
    }

    final override fun getDeclaredConstructor(parameterTypes: List<VascType>): VascConstructor? {
        return declaredConstructors.find { it.isApplicableTo(parameterTypes) }
    }

    final override fun getDeclaredMethod(name: String, parameterTypes: List<VascType>): VascMethod? {
        return declaredMethods.find { it.name == name && it.isApplicableTo(parameterTypes) }
    }

    final override fun getField(name: String): VascVariable? {
        return getDeclaredField(name) ?: parent?.getField(name)
    }

    final override fun getMethod(name: String, parameterTypes: List<VascType>): VascMethod? {
        return getDeclaredMethod(name, parameterTypes) ?: parent?.getMethod(name, parameterTypes)
    }

    final override fun isAssignableFrom(subtype: VascType): Boolean {
        if (subtype is VascNull) return true

        var current: VascType? = subtype
        while (current != null) {
            if (this.name == current.name) return true
            current = current.parent
        }
        return false
    }

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VascType) return false

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}