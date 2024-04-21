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
        classCode.appendLine(" ".repeat(indent) + line) // TODO: add ".line" to trace the source code
    }

    private fun appendLine(line: String, comment: String) {
        appendLine("$line\t\t;/// ${comment.replace("\n", "|")}")
    }

    private fun appendHeader(header: String) {
        classCode.appendLine("; %%% $header %%%")
    }

    private var currentClass: VascClass? = null
    private var currentClassName: String? = null
    private var currentField: VascVariable? = null
    private var currentConstructor: VascConstructor? = null
    private var currentMethod: VascMethod? = null

    private val variableStack = mutableListOf<VascVariable>()

    private val fieldsInitCode = mutableListOf<String>()

// CLASS

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        classCode.clear()
        fieldsInitCode.clear()

        currentClass = typeResolver.visit(ctx.name) as VascClass
        currentClassName = "$classPrefix${currentClass!!.name}"
        appendLine(".class public $currentClassName")
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
        appendLine(".field public ${currentField!!.name} ${currentField!!.type.toJType()}", currentField!!.toString())
        // TODO: generate field init code
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) {
        val params = currentConstructor!!.parameters.joinToString("") { it.type.toJType() }
        appendLine(".method public <init>($params)V", currentConstructor!!.toString())

        indent += indentStep

        appendLine(".limit stack 32") // TODO: calculate limits
        appendLine(".limit locals 32")

        // TODO: add field inits
        // TODO: add super call if not in body

        variableStack.clear()
        variableStack.add(VascVariable("this", currentClass!!))
        variableStack.addAll(currentConstructor!!.parameters)
        ctx.body().accept(this)
        appendLine("return") // TODO: add return only if needed

        indent -= indentStep

        appendLine(".end method")
        appendLine()
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        val params = currentMethod!!.parameters.joinToString("") { it.type.toJType() }
        appendLine(".method public ${currentMethod!!.name}($params)${currentMethod!!.returnType.toJType()}", currentMethod!!.toString())

        indent += indentStep

        appendLine(".limit stack 32")
        appendLine(".limit locals 32")

        variableStack.clear()
        variableStack.add(VascVariable("this", currentClass!!))
        variableStack.addAll(currentMethod!!.parameters)
        ctx.body().accept(this)
        appendLine("return") // TODO: add return only if needed

        indent -= indentStep

        appendLine(".end method")
        appendLine()
    }

    override fun visitBody(ctx: BodyContext) {
        indent += indentStep
        val stackSize = variableStack.size
        super.visitBody(ctx)
        variableStack.dropLast(variableStack.size - stackSize)
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
        appendLine("getfield $booleanClass/value Z")
    }

    override fun visitReturnStatement(ctx: ReturnStatementContext) {
        ctx.expression()?.accept(this)
        appendLine("return", ctx.expression()?.text.toString() ?: "")
    }

    override fun visitAssignStatement(ctx: AssignStatementContext) {
        ctx.expression().accept(this)
        val name = ctx.identifier().text
        val stackIndex = variableStack.indexOfFirst { it.name == name } // TODO: putfield if not local var
        if (stackIndex > 0) {
            appendLine("astore $stackIndex", "assign $name")
        } else {
            val field = currentClass!!.getField(name)!!
            appendLine("putfield $currentClassName/${field.name} ${field.type}", "assign field $field")
        }
    }

    override fun visitPrintStatement(ctx: PrintStatementContext) {
        appendLine("; TODO: print") // TODO
    }

    override fun visitVariableStatement(ctx: VariableStatementContext) {
        ctx.variableDeclaration().let {
            val name = ctx.variableDeclaration().identifier().text
            val type = typeResolver.visit(ctx.variableDeclaration().className())
            val varr = VascVariable(name, type)
            variableStack.add(varr)
            if (it.expression() == null) {
                appendLine("aconst_null")
            } else {
                it.expression().accept(this)
            }
            appendLine("astore ${variableStack.size-1}", "init $varr")
        }
    }

    override fun visitExpressionStatement(ctx: ExpressionStatementContext) {
        ctx.expression().accept(this)
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
        appendLine("new $realClass")
        appendLine("dup")
        appendLine("ldc2_w ${ctx.text}")
        appendLine("invokespecial $realClass/<init>(D)V")
    }

    override fun visitIntegerLiteral(ctx: IntegerLiteralContext) {
        appendLine("new $integerClass")
        appendLine("dup")
        appendLine("ldc2_w ${ctx.text}")
        appendLine("invokespecial $integerClass/<init>(J)V")
    }

    override fun visitFalseLiteral(ctx: FalseLiteralContext) {
        appendLine("new $booleanClass")
        appendLine("dup")
        appendLine("iconst_0")
        appendLine("invokespecial $booleanClass/<init>(Z)V")
    }

    override fun visitTrueLiteral(ctx: TrueLiteralContext) {
        appendLine("new $booleanClass")
        appendLine("dup")
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
