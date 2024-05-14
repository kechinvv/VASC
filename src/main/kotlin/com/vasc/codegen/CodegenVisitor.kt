package com.vasc.codegen

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.member.*
import com.vasc.type.*
import com.vasc.typecheck.PrintString
import com.vasc.typecheck.PrintVar
import com.vasc.typecheck.members
import com.vasc.util.toUniqueVariables
import org.antlr.v4.runtime.ParserRuleContext
import kotlin.math.max

private const val classPrefix = "com/vasc/"

private const val booleanClass = classPrefix + "Boolean"
private const val integerClass = classPrefix + "Integer"
private const val realClass = classPrefix + "Real"

private const val argumentParseSignature = classPrefix + "ArgumentParser/parse([Ljava/lang/String;)[Ljava/lang/Object;"

private const val defaultParent = "java/lang/Object"
private const val erasedType = "L$defaultParent;"

typealias ClassName = String
typealias ClassCode = String

class CodegenVisitor(
    private val typeResolver: VascTypeResolver,
    private val typeTable: MutableMap<ParserRuleContext, VascType>,
    private val errors: MutableList<VascException>,
    private val sourceName: String,
) : VascParserBaseVisitor<Unit>() {

    private val generatedClasses = mutableMapOf<ClassName, ClassCode>()

    fun getGeneratedClasses(): Map<ClassName, ClassCode> {
        return generatedClasses
    }

    private var code = StringBuilder()

    private val indentStep = 2
    private var indent = 0

    private inline fun withIndent(action: () -> Unit) {
        indent += indentStep
        action()
        indent -= indentStep
    }

    private var localLimit = 0

    private inline fun withLimits(action: () -> Unit) {
        localLimit = 0
        withIndent {
            val backup = code
            code = StringBuilder()
            action()
            val temp = code
            code = backup
            appendLine(".limit stack 32") // TODO: calculate limits
            appendLine(".limit locals $localLimit")
            code.append(temp)
        }
    }

    private fun appendLine() {
        code.appendLine()
    }

    private fun appendLine(line: String) {
        code.appendLine(" ".repeat(indent) + line)
    }

    private fun appendLine(line: String, comment: String) {
        appendLine("$line\t\t;/// ${comment.replace("\n", "|")}")
    }

    private fun appendHeader(header: String) {
        appendLine("; %%% $header %%%")
    }

    private var currentClass: VascClass? = null
    private var currentField: VascVariable? = null
    private var currentConstructor: VascConstructor? = null
    private var currentMethod: VascMethod? = null

    private var nextCallType: VascType? = null
    private var specialCall: Boolean = false

    private val variableStack = mutableListOf<VascVariable>()

    private val fieldsInitCode = mutableListOf<String>()

// CLASS

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        code.clear()
        fieldsInitCode.clear()

        currentClass = typeResolver.visit(ctx.name) as VascClass
        appendLine(".source $sourceName")
        appendLine(".class public ${currentClass!!.toJName()}")
        if (currentClass!!.parent == null) {
            appendLine(".super $defaultParent")
        } else {
            appendLine(".super ${currentClass!!.parent!!.toJName()}")
        }
        appendLine()
        ctx.classBody().accept(this)
        generatedClasses[ctx.name.text] = code.toString()
    }

    override fun visitClassBody(ctx: ClassBodyContext) {
        appendHeader("Fields")
        ctx.memberDeclarations.filterIsInstance<FieldDeclarationContext>().map {
            currentField = currentClass!!.getDeclaredField(it.variableDeclaration().identifier().text)!!
            it.accept(this)
        }
        appendHeader("Main")
        generateMain()
        appendHeader("Constructors")
        ctx.memberDeclarations.filterIsInstance<ConstructorDeclarationContext>().forEach { constructor ->
            currentConstructor =  currentClass!!.getDeclaredConstructor(constructor.parameters().toUniqueVariables(typeResolver).map { it.type })!!
            constructor.accept(this)
        }
        if (currentClass!!.declaredConstructors.isEmpty()) {
            appendLine(".method public <init>()V", "generated default constructor")
            withIndent {
                instrLoadThis()
                appendLine("invokespecial ${currentClass?.parent?.toJName() ?: defaultParent}/<init>()V", "call default parent constructor")
                appendLine("return")
            }
            appendLine(".end method")
        }
        appendHeader("Methods")
        ctx.memberDeclarations.filterIsInstance<MethodDeclarationContext>().forEach { method ->
            currentMethod = currentClass!!.getDeclaredMethod(method.identifier().text, method.parameters().toUniqueVariables(typeResolver).map { it.type })!!
            method.accept(this)
        }
        appendLine()
    }

    private fun generateMain() {
        val labels = currentClass!!.declaredConstructors.mapIndexed { i, ctor -> Pair(ctor, "ctor_$i" ) }
        val endLabel = "end"
        val constructorMatchers = mutableMapOf<VascConstructor, String>()
        labels.forEach { (ctor, label) ->
            constructorMatchers[ctor] = generateConstructorMatcher(ctor, label)
        }
        appendLine(".method public static main([Ljava/lang/String;)V")
        withLimits {
            localLimit = 2
            val allLabels = listOf(*labels.map { it.second }.toTypedArray(), endLabel)
            appendLine()
            withIndent {
                appendLine("aload 0", "load args")
                appendLine("invokestatic $argumentParseSignature", "parse args")
                appendLine("astore 1")
                for (i in labels.indices) {
                    val (ctor, label) = labels[i]
                    appendLine("$label:")
                    withIndent {
                        appendLine("aload 1")
                        appendLine("arraylength")
                        appendLine("ldc ${ctor.parameterTypes.size}", "expected number of args")
                        appendLine("if_icmpne ${if (i+1 < labels.size) labels[i+1].second else endLabel}", "skip if number of args differ")

                        appendLine("aload 1")
                        appendLine("invokestatic ${currentClass!!.toJName()}/${constructorMatchers[ctor]!!}")

                        appendLine("ifeq ${allLabels[i+1]}", "continue if constructor did not match")
                        appendLine("return")
                    }
                }
                appendLine("$endLabel:", "no constructor matched")
                withIndent {
                    appendLine("getstatic java/lang/System/out Ljava/io/PrintStream;")
                    appendLine("ldc \"error: no constructors matched arguments\"")
                    appendLine("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V")
                    appendLine("return")
                }
            }
        }
        appendLine(".end method")
    }

    private fun generateConstructorMatcher(ctor: VascConstructor, name: String): String {
        val fullName = "main_$name([Ljava/lang/Object;)Z"
        appendLine(".method public static $fullName")
        withLimits {
            localLimit = 1
            withIndent {
                val className = currentClass!!.toJName()
                val entryLabel = "entry"
                val callLabel = "call"
                val catchLabel = "catch"
                appendLine(".catch java/lang/ClassCastException from $entryLabel to $callLabel using $catchLabel")
                appendLine("$entryLabel:")
                appendLine("new $className")
                for (pi in ctor.parameterTypes.indices) {
                    appendLine("aload 0")
                    appendLine("ldc $pi")
                    appendLine("aaload", "load next arg")
                    appendLine("checkcast ${ctor.parameterTypes[pi].toJName()}")
                }
                appendLine("$callLabel:")
                appendLine("invokespecial $className/<init>(${ctor.parameterTypes.joinToString("") { it.toJType() }})V", "call constructor $ctor")
                appendLine("iconst_1", "ok")
                appendLine("ireturn")
                appendLine("$catchLabel:", "error when casting arguments")
                appendLine("iconst_0")
                appendLine("ireturn")
            }
        }
        appendLine(".end method")
        return fullName
    }

    override fun visitFieldDeclaration(ctx: FieldDeclarationContext) {
        appendLine(".field public \"${currentField!!.name}\" ${currentField!!.type.toJType()}", currentField!!.toString())
        ctx.variableDeclaration().expression()?.let {
            val backupCode = code
            code = StringBuilder() // TODO: need a better solution
            instrLoadThis()
            it.accept(this)
            instrPutField(currentField!!)
            fieldsInitCode.add(code.toString())
            code = backupCode
        }
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) {
        val params = currentConstructor!!.parameters.joinToString("") { it.type.toJType() }
        appendLine(".method public <init>($params)V", currentConstructor!!.toString())
        withLimits {
            if (fieldsInitCode.isNotEmpty()) {
                appendHeader("Init Fields")
                fieldsInitCode.forEach {
                    it.lines().forEach { line ->
                        appendLine(line)
                    }
                }
            }
            val statements = ctx.body().statement()
            if (statements.isEmpty() || !statements[0].let { it is ExpressionStatementContext && it.expression() is SuperExpressionContext }) {
                withIndent {
                    instrLoadThis()
                    appendLine("invokespecial ${currentClass?.parent?.toJName() ?: defaultParent}/<init>()V", "call default parent constructor")
                }
            }
            appendLine()

            variableStack.clear()
            variableStack.add(VascVariable("this", currentClass!!))
            variableStack.addAll(currentConstructor!!.parameters)
            ctx.body().accept(this)
            appendLine("return") // TODO: add return only if needed
        }
        appendLine(".end method")
        appendLine()
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        val params = currentMethod!!.parameters.joinToString("") { it.type.toJType() }
        appendLine(".method public ${currentMethod!!.name}($params)${currentMethod!!.returnType.toJType()}", currentMethod!!.toString())

        withLimits {
            variableStack.clear()
            variableStack.add(VascVariable("this", currentClass!!))
            variableStack.addAll(currentMethod!!.parameters)
            ctx.body().accept(this)
            appendLine("return") // TODO: add return only if needed
        }

        appendLine(".end method")
        appendLine()
    }

    override fun visitBody(ctx: BodyContext) {
        withIndent {
            val stackSize = variableStack.size
            super.visitBody(ctx)
            localLimit = max(localLimit, variableStack.size)
            repeat(variableStack.size - stackSize) {
                variableStack.removeLast()
            }
        }
    }

// STATEMENTS

    override fun visitIfStatement(ctx: IfStatementContext) {
        withLineInfo(ctx.start.line) {
            val endLabel = "If_End_${ctx.pos()}"
            val elseLabel = "If_Else_${ctx.pos()}"
            generateBooleanExpression(ctx.condition)
            if (ctx.elseBody != null) {
                appendLine("ifeq $elseLabel")
                ctx.thenBody.accept(this)
                appendLine("goto $endLabel")
                appendLine("$elseLabel:")
                ctx.elseBody.accept(this)
                appendLine("$endLabel:")
            } else {
                appendLine("ifeq $endLabel")
                ctx.thenBody.accept(this)
                appendLine("$endLabel:")
            }
        }
    }

    override fun visitWhileStatement(ctx: WhileStatementContext) {
        withLineInfo(ctx.start.line) {
            val condLabel = "While_Cond_${ctx.pos()}"
            val endLabel = "While_End_${ctx.pos()}"
            appendLine("$condLabel:")
            generateBooleanExpression(ctx.condition)
            appendLine("ifeq $endLabel")
            ctx.body().accept(this)
            appendLine("goto $condLabel")
            appendLine("$endLabel:")
        }
    }

    private fun generateBooleanExpression(ctx: ExpressionContext) {
        ctx.accept(this)
        appendLine("getfield $booleanClass/value Z")
    }

    override fun visitReturnStatement(ctx: ReturnStatementContext) {
        withLineInfo(ctx.start.line) {
            if ((currentMethod?.returnType ?: VascVoid) == VascVoid) {
                appendLine("return")
            } else {
                ctx.expression().accept(this)
                appendLine("areturn", ctx.expression().text.toString())
            }
        }
    }

    override fun visitAssignStatement(ctx: AssignStatementContext) {
        withLineInfo(ctx.start.line) {
            ctx.expression().accept(this)
            val name = ctx.identifier().text
            val stackIndex = variableStack.indexOfFirst { it.name == name }
            if (stackIndex > 0) {
                appendLine("astore $stackIndex", "assign $name")
            } else {
                val field = currentClass!!.getField(name)!!
                instrLoadThis()
                appendLine("swap")
                instrPutField(field)
            }
        }
    }

    override fun visitPrintStatement(ctx: PrintStatementContext) {
        withLineInfo(ctx.start.line) {
            appendLine("getstatic java/lang/System/out Ljava/io/PrintStream;")
            appendLine("ldc \"\"")
            ctx.members().forEach {  member ->
                when(member) {
                    is PrintString -> {
                        appendLine("ldc \"${member.str}\"")
                    }
                    is PrintVar -> {
                        val stackIndex = variableStack.indexOfFirst { it.name == member.id }
                        if (stackIndex > 0) {
                            appendLine("aload $stackIndex", "read local ${variableStack[stackIndex]}")
                            nextCallType = variableStack[stackIndex].type
                        } else {
                            val field = currentClass!!.getField(member.id)!!
                            instrLoadThis()
                            instrGetField(currentClass!!, field)
                        }
                        appendLine("invokevirtual java/lang/Object/toString()Ljava/lang/String;")
                    }
                }
                appendLine("invokevirtual java/lang/String/concat(Ljava/lang/String;)Ljava/lang/String;")
            }
            appendLine("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V", "next line")
        }
    }

    override fun visitVariableStatement(ctx: VariableStatementContext) {
        withLineInfo(ctx.start.line) {
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
    }

    override fun visitExpressionStatement(ctx: ExpressionStatementContext) {
        withLineInfo(ctx.start.line) {
            ctx.expression().accept(this)
            if (typeTable[ctx.expression()] != VascVoid) {
                appendLine("pop", "discard result")
            }
        }
    }

    private inline fun withLineInfo(line: Int, action: () -> Unit) {
        appendLine(".line $line")
        withIndent {
            action()
        }
    }

// EXPRESSIONS

    override fun visitVariableExpression(ctx: VariableExpressionContext) {
        val name = ctx.identifier().text
        val stackIndex = variableStack.indexOfFirst { it.name == name }
        if (stackIndex > 0) {
            appendLine("aload $stackIndex", "read local ${variableStack[stackIndex]}")
            nextCallType = variableStack[stackIndex].type
            specialCall = false
        } else {
            val field = currentClass!!.getField(name)!!
            instrLoadThis()
            instrGetField(currentClass!!, field)
            nextCallType = field.type
            specialCall = false
        }
        ctx.dotCall().forEach {
            it.accept(this)
        }
    }

    override fun visitCallableExpression(ctx: CallableExpressionContext) {
        val arguments = ctx.arguments().expression().map { typeTable[it]!! }
        val method = currentClass!!.getMethod(ctx.className().text, arguments)
        if (method != null) {
            instrLoadThis()
            ctx.arguments().expression().forEach {
                it.accept(this)
            }
            callMethod(currentClass!!, method)
            nextCallType = method.returnType
            specialCall = false
        } else {
            val cls = typeResolver.visit(ctx.className())
            val constructor = cls.getDeclaredConstructor(arguments)!!
            appendLine("new ${cls.toJName()}")
            appendLine("dup")
            ctx.arguments().expression().forEach {
                it.accept(this)
            }
            val call = "${cls!!.toJName()}/<init>(${constructor.parameterTypes.joinToString("") { it.toJType() }})V"
            appendLine("invokespecial $call", "new $cls.$constructor")
            nextCallType = cls
            specialCall = false
        }
        ctx.dotCall().forEach {
            it.accept(this)
        }
    }

    override fun visitThisExpression(ctx: ThisExpressionContext) {
        if (ctx.arguments() == null) {
            instrLoadThis()
            nextCallType = currentClass
            specialCall = false
            ctx.dotCall().forEach {
                it.accept(this)
            }
        } else {
            val arguments = ctx.arguments().expression().map { typeTable[it]!! }
            val constructor = currentClass!!.getDeclaredConstructor(arguments)!!
            instrLoadThis()
            ctx.arguments().expression().forEach {
                it.accept(this)
            }
            val call = "${currentClass!!.toJName()}/<init>(${constructor.parameterTypes.joinToString("") { it.toJType() }})V"
            appendLine("invokespecial $call", "call constructor $currentClass.$constructor")
            nextCallType = VascVoid
            specialCall = false
        }
    }

    override fun visitSuperExpression(ctx: SuperExpressionContext) {
        if (ctx.arguments() == null) {
            instrLoadThis()
            nextCallType = currentClass!!.parent!!
            specialCall = true
            ctx.dotCall().forEach {
                it.accept(this)
            }
        } else {
            val arguments = ctx.arguments().expression().map { typeTable[it]!! }
            val constructor = currentClass!!.getDeclaredConstructor(arguments)!!
            val cls = currentClass!!.parent!!
            instrLoadThis()
            ctx.arguments().expression().forEach {
                it.accept(this)
            }
            val call = "${cls.toJName()}/<init>(${constructor.parameterTypes.joinToString("") { it.toJType() }})V"
            appendLine("invokespecial $call", "call parent constructor $cls.$constructor")
            nextCallType = VascVoid
            specialCall = false
        }
    }

    override fun visitPrimaryExpression(ctx: PrimaryExpressionContext) {
        ctx.primary().accept(this)
    }

    override fun visitIsNullExpression(ctx: IsNullExpressionContext) {
        ctx.expression().accept(this)
        val endLabel = "IfNull_Merge_${ctx.pos()}"
        val ifNullLabel = "IfNull_${ctx.pos()}"
        appendLine("ifnull $ifNullLabel")
        newFalse()
        appendLine("goto $endLabel")
        appendLine("$ifNullLabel:")
        newTrue()
        appendLine("$endLabel:")
    }

    override fun visitFieldAccess(ctx: FieldAccessContext) {
        val field = nextCallType!!.getField(ctx.identifier().text)
        instrGetField(nextCallType as VascClass, field!!)
        nextCallType = field.type
        specialCall = false
    }

    override fun visitMethodCall(ctx: MethodCallContext) {
        val name = ctx.identifier().text
        val arguments = ctx.arguments().expression().map { typeTable[it]!! }
        val cls = nextCallType!!
        val method = nextCallType!!.getMethod(name, arguments)!!
        val specialCallTemp = specialCall // TODO: this is awkward
        ctx.arguments().expression().forEach {
            it.accept(this)
        }
        specialCall = specialCallTemp
        callMethod(cls, method)
        nextCallType = method.returnType
        specialCall = false
    }

    private fun callMethod(cls: VascType, method: VascMethod) {
        val name = method.name
        var needCast = false
        val call = when (method) {
            is VascGenericMethod -> {
                val arguments = method.maybeGenericParameters.joinToString("") {
                    when (it) {
                        is Generic -> erasedType
                        is Concrete -> it.v.type.toJType()
                    }
                }
                val ret = method.maybeGenericReturnType.let {
                    when (it) {
                        is Generic -> {
                            needCast = true
                            erasedType
                        }
                        is Concrete -> it.v.toJType()
                    }
                }
                "${cls.toJName()}/$name($arguments)$ret"
            }
            else -> {
                val arguments = method.parameters.joinToString("") { it.type.toJType() }
                val ret = method.returnType.toJType()
                "${cls.toJName()}/$name($arguments)$ret"
            }
        }
        if (specialCall) {
            appendLine("invokespecial $call", "call special $cls.$method")
        } else {
            appendLine("invokevirtual $call", "call $cls.$method")
        }
        if (needCast) {
            appendLine("checkcast ${method.returnType.toJName()}")
        }
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

    override fun visitFalseLiteral(ctx: FalseLiteralContext) = newFalse()
    override fun visitTrueLiteral(ctx: TrueLiteralContext) = newTrue()

    private fun newFalse() {
        appendLine("new $booleanClass")
        appendLine("dup")
        appendLine("iconst_0")
        appendLine("invokespecial $booleanClass/<init>(Z)V")
    }

    private fun newTrue() {
        appendLine("new $booleanClass")
        appendLine("dup")
        appendLine("iconst_1")
        appendLine("invokespecial $booleanClass/<init>(Z)V")
    }

    override fun visitNullLiteral(ctx: NullLiteralContext) {
        appendLine("aconst_null")
    }

// HELPERS

    private fun instrGetField(cls: VascClass, field: VascVariable) {
        appendLine("getfield ${cls.toJName()}/${field.name} ${field.type.toJType()}", "read field $cls.$field")
    }

    private fun instrPutField(field: VascVariable) {
        appendLine("putfield ${currentClass!!.toJName()}/${field.name} ${field.type.toJType()}", "assign field $currentClass.$field")
    }

    private fun instrLoadThis() {
        appendLine("aload_0", "load this")
    }

}

private fun removeGenericInfo(name: String): String {
    val typeParamIndex = name.indexOf("[")
    var ret = name
    if (typeParamIndex > 0) {
        ret = ret.substring(0, typeParamIndex)
    }
    return ret
}

private fun VascType.toJType(): String {
    return when (this) {
        is VascVoid -> "V"
        is VascClass -> "L$classPrefix${removeGenericInfo(name)};"
        else -> throw IllegalArgumentException(this.toString())
    }
}

private fun VascType.toJName(): String {
    return when (this) {
        is VascClass -> "$classPrefix${removeGenericInfo(name)}"
        else -> throw IllegalArgumentException(this.toString())
    }
}

private fun ParserRuleContext.pos() = "${start.line}_${start.charPositionInLine}"
