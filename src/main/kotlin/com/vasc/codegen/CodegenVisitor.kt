package com.vasc.codegen

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.member.VascConstructor
import com.vasc.member.VascMethod
import com.vasc.member.VascVariable
import com.vasc.type.*
import com.vasc.util.toUniqueVariables
import org.antlr.v4.runtime.ParserRuleContext

private const val classPrefix = "com/vasc/"

private const val booleanClass = classPrefix + "Boolean"
private const val integerClass = classPrefix + "Integer"
private const val realClass = classPrefix + "Real"
private const val listClass = classPrefix + "List"
private const val arrayClass = classPrefix + "Array"

private typealias ClassName = String
private typealias ClassCode = String

class CodegenVisitor(private val typeResolver: VascTypeResolver, private val errors: MutableList<VascException>) : VascParserBaseVisitor<Unit>() {

    private val generatedClasses = mutableMapOf<ClassName, ClassCode>()

    fun getGeneratedClasses(): Map<ClassName, ClassCode> {
        return generatedClasses
    }

    private val classCode = StringBuilder()

    private val indentStep = 2
    private var indent = 0

    private fun appendLine() {
        classCode.appendLine()
    }

    private fun appendLine(line: String) {
        classCode.appendLine(" ".repeat(indent) + line)
    }

    private fun appendHeader(header: String) {
        classCode.appendLine("; %%% $header %%%")
    }

    private var currentClass: VascClass? = null
    private var currentField: VascVariable? = null
    private var currentConstructor: VascConstructor? = null
    private var currentMethod: VascMethod? = null

    private val fieldsInitCode = mutableListOf<String>()

// CLASS

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        classCode.clear()
        fieldsInitCode.clear()

        currentClass = typeResolver.visit(ctx.name) as VascClass
        appendLine(".class public $classPrefix${currentClass!!.name}")
        if (currentClass!!.parent == null) {
            appendLine(".super java/lang/Object")
        } else {
            appendLine(".super $classPrefix${currentClass!!.parent}")
        }
        appendLine()
        ctx.classBody().accept(this)
        generatedClasses[ctx.name.text] = classCode.toString()
    }

    override fun visitClassBody(ctx: ClassBodyContext) {
        appendHeader("Fields")
        ctx.memberDeclarations.filterIsInstance<FieldDeclarationContext>().map {
            currentField = currentClass!!.getDeclaredField(it.variableDeclaration().identifier().text)!!
            it.accept(this)
        }
        appendHeader("Constructors")
        ctx.memberDeclarations.filterIsInstance<ConstructorDeclarationContext>().forEach { constructor ->
            currentConstructor =  currentClass!!.getDeclaredConstructor(constructor.parameters().toUniqueVariables(typeResolver).map { it.type })!!
            constructor.accept(this)
        }
        // TODO: generate default constructor if needed
        appendHeader("Methods")
        ctx.memberDeclarations.filterIsInstance<MethodDeclarationContext>().forEach { method ->
            currentMethod = currentClass!!.getDeclaredMethod(method.identifier().text, method.parameters().toUniqueVariables(typeResolver).map { it.type })!!
            method.accept(this)
        }
        appendLine()
    }

    override fun visitFieldDeclaration(ctx: FieldDeclarationContext) {
        appendLine(".field public ${currentField!!.name} ${currentField!!.type.toJType()}")
        // TODO: generate field init code
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) {
        val params = currentConstructor!!.parameters.joinToString("") { it.type.toJType() }
        appendLine(".method public <init>($params)V")
        indent += indentStep
        appendLine(".limit stack 32") // TODO: calculate limits
        appendLine(".limit locals 32")
        // TODO: add field inits
        // TODO: add super call if not in body
        ctx.body().accept(this)
        appendLine("return") // TODO: add return only if needed
        indent -= indentStep
        appendLine(".end method")
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        val params = currentMethod!!.parameters.joinToString("") { it.type.toJType() }
        appendLine(".method public ${currentMethod!!.name}($params)${currentMethod!!.returnType.toJType()}")
        indent += indentStep
        appendLine(".limit stack 32")
        appendLine(".limit locals 32")
        ctx.body().accept(this)
        appendLine("return")
        indent -= indentStep
        appendLine(".end method")
    }

    override fun visitBody(ctx: BodyContext) {
        indent += indentStep
        super.visitBody(ctx)
        indent -= indentStep
    }

// STATEMENTS

    override fun visitIfStatement(ctx: IfStatementContext) {
        val endLabel = "If_End_${ctx.pos()}"
        val elseLabel = "If_Else_${ctx.pos()}"
        generateBooleanExpression(ctx.condition)
        if (ctx.elseBody != null) {
            appendLine("ifne $elseLabel")
            ctx.thenBody.accept(this)
            appendLine("goto $endLabel")
            appendLine("$elseLabel:")
            ctx.thenBody.accept(this)
            appendLine("$endLabel:")
        } else {
            appendLine("ifne $endLabel")
            ctx.thenBody.accept(this)
            appendLine("$endLabel:")
        }
    }

    override fun visitWhileStatement(ctx: WhileStatementContext) {
        val condLabel = "While_Cond_${ctx.pos()}"
        val endLabel = "While_End_${ctx.pos()}"
        appendLine("$condLabel:")
        generateBooleanExpression(ctx.condition)
        appendLine("ifne $endLabel")
        ctx.body().accept(this)
        appendLine("goto $condLabel")
        appendLine("$endLabel:")
    }

    private fun generateBooleanExpression(ctx: ExpressionContext) {
        ctx.accept(this)
        appendLine("getfield $booleanClass Z")
    }

    override fun visitReturnStatement(ctx: ReturnStatementContext) {
        ctx.expression()?.accept(this)
        appendLine("return")
    }

    override fun visitAssignStatement(ctx: AssignStatementContext) {
        appendLine("; TODO: assign") // TODO
    }

    override fun visitPrintStatement(ctx: PrintStatementContext) {
        appendLine("; TODO: print") // TODO
    }

    override fun visitVariableStatement(ctx: VariableStatementContext) {
        appendLine("; TODO: variable") // TODO
    }

    override fun visitExpressionStatement(ctx: ExpressionStatementContext) {
        appendLine("; TODO: expression") // TODO
    }

// EXPRESSIONS

    override fun visitVariableExpression(ctx: VariableExpressionContext) {
        appendLine("; TODO: variable expression") // TODO
    }

    override fun visitCallableExpression(ctx: CallableExpressionContext) {
        appendLine("; TODO: callable expression") // TODO
    }

    override fun visitThisExpression(ctx: ThisExpressionContext) {
        appendLine("; TODO: this expression") // TODO
    }

    override fun visitSuperExpression(ctx: SuperExpressionContext) {
        appendLine("; TODO: super expression") // TODO
    }

    override fun visitPrimaryExpression(ctx: PrimaryExpressionContext) {
        ctx.primary().accept(this)
    }

// PRIMARY

    override fun visitRealLiteral(ctx: RealLiteralContext) {
        appendLine("new $integerClass")
        appendLine("ldc2_w ${ctx.text}")
        appendLine("invokespecial $realClass/<init>(D)V")
    }

    override fun visitIntegerLiteral(ctx: IntegerLiteralContext) {
        appendLine("new $integerClass")
        appendLine("ldc2_w ${ctx.text}")
        appendLine("invokespecial $integerClass/<init>(J)V")
    }

    override fun visitFalseLiteral(ctx: FalseLiteralContext) {
        appendLine("new $booleanClass")
        appendLine("iconst_0")
        appendLine("invokespecial $booleanClass/<init>(Z)V")
    }

    override fun visitTrueLiteral(ctx: TrueLiteralContext) {
        appendLine("new $booleanClass")
        appendLine("iconst_1")
        appendLine("invokespecial $booleanClass/<init>(Z)V")
    }

    override fun visitNullLiteral(ctx: NullLiteralContext) {
        appendLine("aconst_null")
    }

}

private fun VascType.toJType(): String {
    return when (this) {
        is VascVoid -> "V"
        is VascClass -> {
            val typeParamIndex = name.indexOf("[")
            var name = name
            if (typeParamIndex > 0) {
                name = name.substring(0, typeParamIndex)
            }
            "L$classPrefix$name;"
        }
        else -> throw IllegalArgumentException(this.toString())
    }
}

private fun ParserRuleContext.pos() = "${start.line}_${start.charPositionInLine}"
