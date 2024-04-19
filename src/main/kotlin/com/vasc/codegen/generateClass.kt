package com.vasc.codegen

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.error.VascException
import com.vasc.member.VascConstructor
import com.vasc.member.VascMethod
import com.vasc.member.VascVariable
import com.vasc.type.*
import com.vasc.util.toUniqueVariables

private const val classPrefix = "vasc/"

class ClassCodeGenerator(private val typeResolver: VascTypeResolver, private val errors: MutableList<VascException>) {
    private val code = StringBuilder()

    private fun appendLine() {
        code.appendLine()
    }

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
        appendLine()
        generateClassBody(vascClass, ctx.classBody())
        return code.toString()
    }

    private fun generateClassBody(vascClass: VascClass, ctx: ClassBodyContext) {
        val fieldInits = ctx.memberDeclarations.filterIsInstance<FieldDeclarationContext>().map {
            generateField(
                vascClass.getDeclaredField(it.variableDeclaration().identifier().text)!!,
                it
            )
        }
        appendLine()
        ctx.memberDeclarations.filterIsInstance<ConstructorDeclarationContext>().forEach { ctor ->
            generateConstructor(
                vascClass.getDeclaredConstructor(ctor.parameters().toUniqueVariables(typeResolver).map { it.type })!!,
                ctor
            )
        }
        appendLine()
        ctx.memberDeclarations.filterIsInstance<MethodDeclarationContext>().forEach { method ->
            generateMethod(
                vascClass.getDeclaredMethod(method.identifier().text, method.parameters().toUniqueVariables(typeResolver).map { it.type })!!,
                method
            )
        }
        appendLine()
    }

    private fun generateField(vascVariable: VascVariable, ctx: FieldDeclarationContext): Pair<VascVariable, ExpressionContext>? {
        appendLine(".field public ${vascVariable.name} ${vascVariable.type.toJType()}")
        return ctx.variableDeclaration().expression()?.let { Pair(vascVariable, it) }
    }

    private fun generateConstructor(vascConstructor: VascConstructor, ctx: ConstructorDeclarationContext) {
        val params = vascConstructor.parameters.joinToString("") { it.type.toJType() }
        appendLine(".method public <init>($params)V")
        generateBody(ctx.body())
        appendLine(".end method")
    }

    private fun generateMethod(vascMethod: VascMethod, ctx: MethodDeclarationContext) {
        val params = vascMethod.parameters.joinToString("") { it.type.toJType() }
        appendLine(".method public ${vascMethod.name}($params)${vascMethod.returnType.toJType()}")
        generateBody(ctx.body())
        appendLine(".end method")
    }

    private fun generateBody(ctx: BodyContext) {
    }

}

private fun VascType.toJType(): String {
    return when (this) {
        is VascVoid -> "V"
        is VascReal -> "D"
        is VascInteger -> "J"
        is VascBoolean -> "Z"
        is VascClass -> "L$classPrefix$name;" // TODO: fix generics
        else -> throw IllegalArgumentException(this.toString())
    }
}
