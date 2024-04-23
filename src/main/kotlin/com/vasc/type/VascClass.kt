package com.vasc.type

import com.vasc.member.*

open class VascClass(override val name: String) : VascType {

    override val parent: VascType? = null

    override val declaredFields: Collection<VascVariable> = emptySet()
    override val declaredConstructors: Collection<VascConstructor> = emptySet()
    override val declaredMethods: Collection<VascMethod> = emptySet()

    override val fields: Collection<VascVariable>
        get() = declaredFields + (parent?.fields ?: emptyList())

    override val methods: Collection<VascMethod>
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
        if (other !is VascClass) return false

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}