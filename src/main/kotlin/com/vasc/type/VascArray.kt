package com.vasc.type

import com.vasc.member.VascConstructor
import com.vasc.member.VascConstructor.Companion.`this`
import com.vasc.member.VascMethod
import com.vasc.member.VascVariable.Companion.colon

class VascArray(val genericType: VascType) : VascClass("Array[${genericType.name}]") {

    override val declaredConstructors: List<VascConstructor> = listOf(
        `this`("l" colon VascInteger)
    )

    override val declaredMethods: List<VascMethod> = listOf(
        VascMethod("toList", emptyList(), VascList(genericType)),
        VascMethod("length", emptyList(), VascInteger),
        VascMethod("get", listOf("i" colon VascInteger), genericType),
        VascMethod("set", listOf("i" colon VascInteger, "v" colon genericType), VascVoid)
    )

}