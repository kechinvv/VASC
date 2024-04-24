package com.vasc.type

import com.vasc.member.*
import com.vasc.member.VascConstructor.Companion.`this`
import com.vasc.member.VascVariable.Companion.colon

class VascArray(val genericType: VascType) : VascClass("Array[${genericType.name}]") {

    override val declaredConstructors: List<VascConstructor> = listOf(
        `this`("l" colon VascInteger)
    )

    override val declaredMethods: List<VascMethod> = listOf(
        VascMethod("toList", emptyList(), VascList(genericType)),
        VascMethod("length", emptyList(), VascInteger),
        VascGenericMethod("get", listOf(Concrete("i" colon VascInteger)), Generic(genericType)),
        VascGenericMethod("set", listOf(Concrete("i" colon VascInteger), Generic("v" colon genericType)), Concrete(VascVoid))
    )

}