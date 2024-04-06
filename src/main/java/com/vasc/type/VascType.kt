package com.vasc.type

import com.vasc.member.*

interface VascType {
    val name: String
    val parent: VascType?
    val fields: Set<VascVariable>
    val methods: Set<VascMethod>
    val constructors: Set<VascConstructor>

    fun getField(name: String): VascVariable?
    fun getMethods(name: String): Set<VascMethod>
}