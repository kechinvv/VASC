package com.vasc.type

import com.vasc.member.VascConstructor
import com.vasc.member.VascConstructor.Companion.`this`
import com.vasc.member.VascMethod
import com.vasc.member.VascVariable.Companion.colon

object VascBoolean : VascClass("Boolean") {

    override val declaredConstructors: List<VascConstructor> = listOf(
        `this`("v" colon VascBoolean)
    )

    override val declaredMethods: List<VascMethod> = listOf(
        VascMethod("toInteger", listOf("p" colon VascBoolean), VascBoolean),

        VascMethod("or", listOf("p" colon VascBoolean), VascBoolean),
        VascMethod("and", listOf("p" colon VascBoolean), VascBoolean),
        VascMethod("xor", listOf("p" colon VascBoolean), VascBoolean),
        VascMethod("not", emptyList(), VascBoolean),
    )

}