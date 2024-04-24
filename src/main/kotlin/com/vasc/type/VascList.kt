package com.vasc.type

import com.vasc.member.*
import com.vasc.member.VascConstructor.Companion.`this`
import com.vasc.member.VascVariable.Companion.colon

class VascList(val genericType: VascType) : VascClass("List[${genericType.name}]") {

    override val declaredConstructors: List<VascConstructor> = listOf(
        `this`(),
        `this`("p" colon genericType),
        `this`("p" colon genericType, "count" colon VascInteger)
    )

    override val declaredMethods: List<VascMethod> = listOf(
        VascGenericMethod("append", listOf(Generic("v" colon genericType)), Concrete(this)),
        VascGenericMethod("head", emptyList(), Generic(genericType)),
        VascGenericMethod("tail", emptyList(), Concrete(this)),
        VascMethod("isEmpty", emptyList(), VascBoolean)
    )

}