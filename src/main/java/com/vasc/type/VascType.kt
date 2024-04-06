package com.vasc.type

import com.vasc.member.*

interface VascType {
    val name: String
    val parent: VascType?

    val declaredFields: List<VascVariable>
    val declaredConstructors: List<VascConstructor>
    val declaredMethods: List<VascMethod>

    val fields: List<VascVariable>
    val methods: List<VascMethod>

    fun getDeclaredField(name: String): VascVariable?
    fun getDeclaredConstructor(parameterTypes: List<VascType>): VascConstructor?
    fun getDeclaredMethod(name: String, parameterTypes: List<VascType>): VascMethod?

    fun getField(name: String): VascVariable?
    fun getMethod(name: String, parameterTypes: List<VascType>): VascMethod?
}