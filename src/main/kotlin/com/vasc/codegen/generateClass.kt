package com.vasc.codegen

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.error.VascException
import com.vasc.member.VascConstructor
import com.vasc.member.VascMethod
import com.vasc.member.VascVariable
import com.vasc.type.*
import com.vasc.util.toUniqueVariables
import org.antlr.v4.runtime.ParserRuleContext

private const val classPrefix = "com/vasc/"

class ClassCodeGenerator(private val typeResolver: VascTypeResolver, private val errors: MutableList<VascException>) {
    private val code = StringBuilder()

    private val indentStep = 2
    private var indent = 0

    private fun appendLine() {
        code.appendLine()
    }

    private fun appendLine(line: String) {
        code.appendLine(" ".repeat(indent) + line)
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
        appendLine("; %%% Fields %%%")
        val fieldInits = ctx.memberDeclarations.filterIsInstance<FieldDeclarationContext>().map {
            generateField(
                vascClass.getDeclaredField(it.variableDeclaration().identifier().text)!!,
                it
            )
        }
        appendLine("; %%% Constructors %%%")
        ctx.memberDeclarations.filterIsInstance<ConstructorDeclarationContext>().forEach { ctor ->
            generateConstructor(
                vascClass.getDeclaredConstructor(ctor.parameters().toUniqueVariables(typeResolver).map { it.type })!!,
                ctor
            )
        }
        appendLine("; %%% Methods %%%")
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
        indent += indentStep
        for (st in ctx.statement()) {
            when (st) {
                is IfStatementContext -> {
                    generateExpression(st.condition)
                    val endLabel = "If_End_${st.pos()}"
                    if (st.elseBody != null) {
                        val elseLabel = "If_Else_${st.pos()}"
                        appendLine("ifne $elseLabel")
                        generateBody(st.thenBody)

                        appendLine("goto $endLabel")

                        appendLine("$elseLabel:")
                        generateBody(st.elseBody)

                        appendLine("$endLabel:")
                    } else {
                        appendLine("ifne $endLabel")
                        generateBody(st.thenBody)
                        appendLine("$endLabel:")
                    }
                }
                is WhileStatementContext -> {
                    val condLabel = "While_Cond_${st.pos()}"
                    appendLine("$condLabel:")

                    generateExpression(st.condition)
                    val endLabel = "While_End_${st.pos()}"
                    appendLine("ifne $endLabel")

                    generateBody(st.body())

                    appendLine("goto $condLabel")
                    appendLine("$endLabel:")
                }
                is AssignStatementContext -> {
                    appendLine("; TODO: assign")
                }
                is ReturnStatementContext -> {
                    st.expression()?.let { generateExpression(it) }
                    appendLine("return")
                }
                is ExpressionStatementContext -> {
                    generateExpression(st.expression())
                }
                is PrintStatementContext -> {
                    appendLine("; TODO: print")
                }
                is VariableStatementContext -> {
                    appendLine("; TODO: var")
                }
            }
        }
        indent -= indentStep
    }

    private fun generateExpression(ctx: ExpressionContext) {
        appendLine("; TODO: expression")
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

private fun ParserRuleContext.pos() = "${start.line}_${start.charPositionInLine}"
