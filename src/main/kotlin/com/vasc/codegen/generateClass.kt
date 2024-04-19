package com.vasc.codegen

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.error.VascException
import com.vasc.member.VascVariable
import com.vasc.type.*

private val classPrefix = "vasc/"

class ClassCodeGenerator(private val typeResolver: VascTypeResolver, private val errors: MutableList<VascException>) {
    private val code = StringBuilder()

    private fun appendLine(line: String) {
        code.appendLine(line)
    }

    override fun toString() = code.toString()

    fun generateClass(ctx: ClassDeclarationContext): String {
        code.clear()
        val vascClass = typeResolver.visit(ctx.name) as VascClass
        appendLine(".class public $classPrefix${vascClass.name}")
        if (vascClass.parent == null) {
            appendLine(".super java/lang/Object")
        } else {
            appendLine(".super $classPrefix${vascClass.parent}") // TODO: check that parent != Integer/Real/Bool
        }
        generateClassBody(vascClass, ctx.classBody())
        return code.toString()
    }

    private fun generateClassBody(vascClass: VascClass, ctx: ClassBodyContext) {
         ctx.memberDeclarations.filterIsInstance<FieldDeclarationContext>().forEach {
             generateField(vascClass.getDeclaredField(it.variableDeclaration().identifier().text)!!, it)
         }
    }

    private fun generateField(vascVariable: VascVariable, ctx: FieldDeclarationContext) {
        appendLine(".field public ${vascVariable.name} ${vascVariable.type.toJType()}")
    }

}

private fun VascType.toJType(): String {
    return when (this) {
        is VascVoid -> "V"
        is VascReal -> "D"
        is VascInteger -> "J"
        is VascBoolean -> "Z"
        is VascClass -> "$classPrefix$name"
        else -> throw IllegalArgumentException(this.toString())
    }
}
