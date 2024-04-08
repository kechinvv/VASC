package com.vasc.type

import com.vasc.member.VascConstructor
import com.vasc.member.VascConstructor.Companion.`this`
import com.vasc.member.VascMethod
import com.vasc.member.VascVariable.Companion.colon

object VascInteger : VascClass("Integer") {

    override val declaredConstructors: List<VascConstructor> = listOf(
        `this`("p" colon VascInteger),
        `this`("p" colon VascReal)
    )

    override val declaredMethods: List<VascMethod> = listOf(
        VascMethod("toReal", emptyList(), VascReal),
        VascMethod("toBoolean", emptyList(), VascBoolean),

        VascMethod("unaryMinus", emptyList(), VascInteger),

        VascMethod("plus", listOf("p" colon VascInteger), VascInteger),
        VascMethod("plus", listOf("p" colon VascReal), VascReal),
        VascMethod("minus", listOf("p" colon VascInteger), VascInteger),
        VascMethod("minus", listOf("p" colon VascReal), VascReal),
        VascMethod("mul", listOf("p" colon VascInteger), VascInteger),
        VascMethod("mul", listOf("p" colon VascReal), VascReal),
        VascMethod("div", listOf("p" colon VascInteger), VascInteger),
        VascMethod("div", listOf("p" colon VascReal), VascReal),
        VascMethod("rem", listOf("p" colon VascInteger), VascInteger),

        VascMethod("less", listOf("p" colon VascInteger), VascBoolean),
        VascMethod("less", listOf("p" colon VascReal), VascBoolean),
        VascMethod("lessEqual", listOf("p" colon VascInteger), VascBoolean),
        VascMethod("lessEqual", listOf("p" colon VascReal), VascBoolean),
        VascMethod("greater", listOf("p" colon VascInteger), VascBoolean),
        VascMethod("greater", listOf("p" colon VascReal), VascBoolean),
        VascMethod("greaterEqual", listOf("p" colon VascInteger), VascBoolean),
        VascMethod("greaterEqual", listOf("p" colon VascReal), VascBoolean),
        VascMethod("equal", listOf("p" colon VascInteger), VascBoolean),
        VascMethod("equal", listOf("p" colon VascReal), VascBoolean),
    )

}