package com.vasc.typecheck

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.type.*
import com.vasc.util.toUniqueVariables
import org.antlr.v4.runtime.ParserRuleContext

class TypeChecker(
    private val typeResolver: VascTypeResolver,
    private val currentScope: Scope,
    private val typeTable: MutableMap<ParserRuleContext, VascType>
) : VascParserBaseVisitor<Unit>() {

// LITERALS

    override fun visitRealLiteral(ctx: RealLiteralContext) {
        typeTable[ctx] = VascReal
    }

    override fun visitFalseLiteral(ctx: FalseLiteralContext) {
        typeTable[ctx] = VascBoolean
    }

    override fun visitTrueLiteral(ctx: TrueLiteralContext) {
        typeTable[ctx] = VascBoolean
    }

    override fun visitIntegerLiteral(ctx: IntegerLiteralContext) {
        typeTable[ctx] = VascInteger
    }

// STATEMENTS

    override fun visitWhileStatement(ctx: WhileStatementContext) {
        val expectedT = VascBoolean
        val actualT = ctx.expression().let {
            it.accept(this)
            typeTable[it]!!
        }
        if (expectedT != actualT) {
            throw UnexpectedTypeException(expectedT, actualT, ctx.expression())
        }
        ctx.body().accept(copy(currentScope.enclosed()))
    }

    override fun visitAssignStatement(ctx: AssignStatementContext) {
        val expectedT = ctx.identifier().let {
            currentScope.find(it.text) ?: throw UnknownVariableException(it.text, ctx)
        }
        val actualT = ctx.expression().let {
            it.accept(this)
            typeTable[it]!!
        }
        if (!expectedT.isAssignableFrom(actualT)) {
            throw UnexpectedTypeException(expectedT, actualT, ctx)
        }
    }

    override fun visitIfStatement(ctx: IfStatementContext) {
        val expectT = VascBoolean
        val actualT = ctx.expression().let {
            it.accept(this)
            typeTable[it]!!
        }
        if (expectT != actualT) {
            throw UnexpectedTypeException(expectT, actualT, ctx.expression())
        }
        ctx.thenBody.accept(copy(currentScope.enclosed()))
        ctx.elseBody?.accept(copy(currentScope.enclosed()))
    }

    override fun visitVariableStatement(ctx: VariableStatementContext) {
        val v = ctx.variableDeclaration()
        v.accept(this)
        currentScope.add(v.identifier().text, typeResolver.visit(ctx.variableDeclaration().className()))
    }

    override fun visitReturnStatement(ctx: ReturnStatementContext) {
        val expectedT = currentScope.returnT() ?: throw UnnecessaryReturnException(ctx)
        val actualT = ctx.expression().let {
            it.accept(this)
            typeTable[it]!!
        }
        if (!expectedT.isAssignableFrom(actualT)) {
            throw UnexpectedTypeException(actualT, expectedT, ctx)
        }
    }

// DECLARATIONS

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        val classT = typeResolver.visit(ctx.name)!!
        val bodyCtx = ctx.classBody()
        val (fields, nonFields) = bodyCtx.memberDeclarations.partition { it is FieldDeclarationContext }
        fields.forEach {
            it.accept(this)
        }
        val enclosed = copy(
            currentScope.enclosed(
                vars = classT.fields.associate { it.name to it.type }.toMutableMap(),
                classT = classT
            )
        )
        enclosed.currentScope.add("this", classT)
        classT.parent?.let {
            enclosed.currentScope.add("super", it)
        }
        nonFields.forEach {
            it.accept(enclosed)
        }
    }

    override fun visitVariableDeclaration(ctx: VariableDeclarationContext) {
        val expectedT = typeResolver.visit(ctx.className())
        ctx.expression()?.let {
            it.accept(this)
            val actualT = typeTable[it]!!
            if (!expectedT.isAssignableFrom(actualT)) {
                throw UnexpectedTypeException(expectedT, actualT, ctx)
            }
        }
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) {
        val params = ctx.parameters().toUniqueVariables(typeResolver)
        val enclosed = copy(currentScope.enclosed(params.associate { it.name to it.type }.toMutableMap()))
        ctx.body().accept(enclosed)
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        val params = ctx.parameters().toUniqueVariables(typeResolver)
        val enclosed = copy(
            currentScope.enclosed(
                vars = params.associate { it.name to it.type }.toMutableMap(),
                returnT = currentScope.classT()!!.getDeclaredMethod(ctx.identifier().text, params.map { it.type })!!.returnType
            )
        )
        ctx.body().accept(enclosed)
    }

// EXPRESSIONS

    override fun visitCallableExpression(ctx: CallableExpressionContext) {
        ctx.arguments().expression().forEach { it.accept(this) }
        val name = ctx.className().text
        val args = ctx.arguments().expression().map { typeTable[it]!! }
        val initT =
            currentScope.classT()?.getMethod(name, args)?.returnType ?:
            typeResolver.visit(ctx.className())!!.let {
                it.getDeclaredConstructor(args) ?: throw ConstructorNotFoundException(it.name, args, ctx)
                it
            }
        dotCall(initT, ctx.dotCall())?.let {
            typeTable[ctx] = it
        }
    }

    override fun visitSuperExpression(ctx: SuperExpressionContext) {
        val initT = currentScope.classT()!!.let {
            it.parent ?: throw ParentNotFoundException(it.name, ctx)
        }
        ctx.arguments()?.let { argsCtx ->
            val args = argsCtx.expression().map {
                it.accept(this)
                typeTable[it]!!
            }
            initT.getDeclaredConstructor(args) ?: throw ConstructorNotFoundException(initT.name, args, ctx)
        }
        dotCall(initT, ctx.dotCall())?.let {
            typeTable[ctx] = it
        }
    }

    override fun visitThisExpression(ctx: ThisExpressionContext) {
        val initT = currentScope.classT()!!
        ctx.arguments()?.let { argsCtx ->
            val args = argsCtx.expression().map {
                it.accept(this)
                typeTable[it]!!
            }
            initT.getDeclaredConstructor(args) ?: throw ConstructorNotFoundException(initT.name, args, ctx)
        }
        dotCall(initT, ctx.dotCall())?.let {
            typeTable[ctx] = it
        }
    }

    override fun visitVariableExpression(ctx: VariableExpressionContext) {
        val initT = ctx.identifier().let {
            currentScope.find(it.text) ?: throw UnknownVariableException(it.text, ctx)
        }
        dotCall(initT, ctx.dotCall())?.let {
            typeTable[ctx] = it
        }
    }

    override fun visitPrimaryExpression(ctx: PrimaryExpressionContext) {
        typeTable[ctx] = ctx.primary().let {
            it.accept(this)
            typeTable[it]!!
        }
    }

    private fun dotCall(initT: VascType, calls: List<DotCallContext>): VascType? {
        var nextT: VascType? = initT
        for (nextCall in calls) {
            when(nextCall) {
                is FieldAccessContext -> {
                    nextT = nextT!!.getField(nextCall.identifier().text)!!.type
                }
                is MethodCallContext -> {
                    val args = nextCall.arguments().expression().map {
                        it.accept(this)
                        typeTable[it]!!
                    }
                    val name = nextCall.identifier().text
                    val method = nextT!!.getMethod(name, args) ?: throw MethodNotFoundException(nextT.name, name, args, nextCall)
                    val result = method.returnType
                    if (result == null && nextCall != calls.last()) {
                        throw throw MethodReturnsNoValueException(nextT.name, method.name, nextCall)
                    }
                    nextT = result
                }
            }
        }
        return nextT
    }


    private fun copy(enclosedScope: Scope) = TypeChecker(this.typeResolver, enclosedScope, this.typeTable)
}
