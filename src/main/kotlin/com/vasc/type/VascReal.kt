package com.vasc.type

import com.vasc.member.VascConstructor
import com.vasc.member.VascConstructor.Companion.`this`
import com.vasc.member.VascMethod
import com.vasc.member.VascVariable.Companion.colon

object VascReal : VascClass("Real") {

    override val declaredConstructors: List<VascConstructor> = listOf(
        `this`("p" colon VascReal),
        `this`("p" colon VascInteger)
    )

    override val declaredMethods: List<VascMethod> = listOf(
        VascMethod("toInteger", emptyList(), VascInteger),

        VascMethod("unaryMinus", emptyList(), VascInteger),
        VascMethod("sqrt", emptyList(), VascReal),

        VascMethod("plus", listOf("p" colon VascReal), VascReal),
        VascMethod("plus", listOf("p" colon VascInteger), VascReal),
        VascMethod("minus", listOf("p" colon VascReal), VascReal),
        VascMethod("minus", listOf("p" colon VascInteger), VascReal),
        VascMethod("mul", listOf("p" colon VascReal), VascReal),
        VascMethod("mul", listOf("p" colon VascInteger), VascReal),
        VascMethod("div", listOf("p" colon VascReal), VascReal),
        VascMethod("div", listOf("p" colon VascInteger), VascReal),
        VascMethod("rem", listOf("p" colon VascInteger), VascReal),

        VascMethod("less", listOf("p" colon VascReal), VascBoolean),
        VascMethod("less", listOf("p" colon VascInteger), VascBoolean),
        VascMethod("lessEqual", listOf("p" colon VascReal), VascBoolean),
        VascMethod("lessEqual", listOf("p" colon VascInteger), VascBoolean),
        VascMethod("greater", listOf("p" colon VascReal), VascBoolean),
        VascMethod("greater", listOf("p" colon VascInteger), VascBoolean),
        VascMethod("greaterEqual", listOf("p" colon VascReal), VascBoolean),
        VascMethod("greaterEqual", listOf("p" colon VascInteger), VascBoolean),
        VascMethod("equal", listOf("p" colon VascReal), VascBoolean),
        VascMethod("equal", listOf("p" colon VascInteger), VascBoolean),
    )

}