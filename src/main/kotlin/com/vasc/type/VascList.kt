package com.vasc.type

import com.vasc.member.VascConstructor
import com.vasc.member.VascConstructor.Companion.`this`
import com.vasc.member.VascMethod
import com.vasc.member.VascVariable.Companion.colon

class VascList(val genericType: VascType) : VascClass("List[${genericType.name}]") {

    override val declaredConstructors: List<VascConstructor> = listOf(
        `this`(),
        `this`("p" colon genericType),
        `this`("p" colon genericType, "count" colon VascInteger)
    )

    override val declaredMethods: List<VascMethod> = listOf(
        VascMethod("append", listOf("v" colon genericType), this),
        VascMethod("head", emptyList(), genericType),
        VascMethod("tail", emptyList(), this),
        VascMethod("isEmpty", emptyList(), VascBoolean)
    )

}