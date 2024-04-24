package com.vasc.type

import com.vasc.member.*

interface VascType {
    val name: String
    val parent: VascType?

    val declaredFields: Collection<VascVariable>
    val declaredConstructors: Collection<VascConstructor>
    val declaredMethods: Collection<VascMethod>

    val fields: Collection<VascVariable>
    val methods: Collection<VascMethod>

    fun getDeclaredField(name: String): VascVariable?
    fun getDeclaredConstructor(parameterTypes: List<VascType>): VascConstructor?
    fun getDeclaredMethod(name: String, parameterTypes: List<VascType>): VascMethod?

    fun getField(name: String): VascVariable?
    fun getMethod(name: String, parameterTypes: List<VascType>): VascMethod?

    /**
     * @see java.lang.Class.isAssignableFrom
     */
    fun isAssignableFrom(subtype: VascType): Boolean
}