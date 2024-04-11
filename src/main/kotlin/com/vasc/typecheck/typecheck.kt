package com.vasc.typecheck

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.type.*
import com.vasc.util.toUniqueVariables
import org.antlr.v4.runtime.ParserRuleContext

class TypeChecker(
    private val typeResolver: VascTypeResolver,
    private val scope: Scope,
    private val typeTable: MutableMap<ParserRuleContext, VascType>,
    private val returnT: VascType = VascVoid,
    private val classT: VascType = VascVoid,
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

    override fun visitNullLiteral(ctx: NullLiteralContext) {
        typeTable[ctx] = VascNull
    }

// STATEMENTS

    override fun visitWhileStatement(ctx: WhileStatementContext) {
        val expectedT = VascBoolean
        val actualT = ctx.expression().let {
            it.accept(this)
            typeTable[it] ?: throw ExpressionHasNoValueException(it)
        }
        if (expectedT != actualT) {
            throw UnexpectedTypeException(expectedT, actualT, ctx.expression())
        }
        ctx.body().accept(copy(scope.enclosed()))
    }

    override fun visitAssignStatement(ctx: AssignStatementContext) {
        val expectedT = ctx.identifier().let {
            scope.find(it.text) ?: throw UnknownVariableException(it.text, it)
        }
        val actualT = ctx.expression().let {
            it.accept(this)
            typeTable[it] ?: throw ExpressionHasNoValueException(it)
        }
        if (!expectedT.isAssignableFrom(actualT)) {
            throw UnexpectedTypeException(expectedT, actualT, ctx)
        }
    }

    override fun visitIfStatement(ctx: IfStatementContext) {
        val expectT = VascBoolean
        val actualT = ctx.expression().let {
            it.accept(this)
            typeTable[it] ?: throw ExpressionHasNoValueException(it)
        }
        if (expectT != actualT) {
            throw UnexpectedTypeException(expectT, actualT, ctx.expression())
        }
        ctx.thenBody.accept(copy(scope.enclosed()))
        ctx.elseBody?.accept(copy(scope.enclosed()))
    }

    override fun visitVariableStatement(ctx: VariableStatementContext) {
        val v = ctx.variableDeclaration()
        v.accept(this)
        scope.add(v.identifier().text, typeResolver.visit(ctx.variableDeclaration().className()))
    }

    override fun visitReturnStatement(ctx: ReturnStatementContext) {
        val expectedT = returnT
        if (ctx.expression() == null) {
            if (expectedT != VascVoid) {
                throw UnexpectedTypeException(expectedT, VascVoid, ctx)
            }
        } else {
            val actualT = ctx.expression().let {
                it.accept(this)
                typeTable[it] ?: throw ExpressionHasNoValueException(it)
            }
            if (!expectedT.isAssignableFrom(actualT)) {
                throw UnexpectedTypeException(actualT, expectedT, ctx)
            }
        }
    }

// DECLARATIONS

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        val classT = typeResolver.visit(ctx.name)!!
        val bodyCtx = ctx.classBody()
        val enclosed = copy(
            scope.enclosed(
                vars = classT.fields.associate { it.name to it.type }.toMutableMap(),
            ),
            enclosedClassT = classT
        )
        enclosed.scope.add("this", classT)
        classT.parent?.let {
            enclosed.scope.add("super", it)
        }
        bodyCtx.memberDeclarations.forEach {
            it.accept(enclosed)
        }
    }

    override fun visitVariableDeclaration(ctx: VariableDeclarationContext) {
        val expectedT = typeResolver.visit(ctx.className())
        ctx.expression()?.let {
            it.accept(this)
            val actualT = typeTable[it] ?: throw ExpressionHasNoValueException(it)
            if (!expectedT.isAssignableFrom(actualT)) {
                throw UnexpectedTypeException(expectedT, actualT, ctx)
            }
        }
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) {
        val params = ctx.parameters().toUniqueVariables(typeResolver)
        val enclosed = copy(scope.enclosed(params.associate { it.name to it.type }.toMutableMap()))
        ctx.body().accept(enclosed)
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        val params = ctx.parameters().toUniqueVariables(typeResolver)
        val enclosed = copy(
            enclosedScope = scope.enclosed(
                vars = params.associate { it.name to it.type }.toMutableMap(),
            ),
            enclosedReturnT = classT.getDeclaredMethod(ctx.identifier().text, params.map { it.type })!!.returnType
        )
        ctx.body().accept(enclosed)
    }

// EXPRESSIONS

    override fun visitCallableExpression(ctx: CallableExpressionContext) {
        ctx.arguments().expression().forEach { it.accept(this) }
        val name = ctx.className().text
        val args = ctx.arguments().expression().map {
            typeTable[it] ?: throw ExpressionHasNoValueException(it)
        }
        val methodCandidate = classT.getMethod(name, args)
        val initT = methodCandidate?.returnType
            ?: typeResolver.visit(ctx.className()).let {
                it.getDeclaredConstructor(args) ?: throw ConstructorNotFoundException(it.name, args, ctx)
                it
            }
        typeTable[ctx] = dotCall(initT, ctx.dotCall())
    }

    override fun visitSuperExpression(ctx: SuperExpressionContext) {
        val initT = classT.let {
            it.parent ?: throw ParentNotFoundException(it.name, ctx)
        }
        ctx.arguments()?.let { argsCtx ->
            val args = argsCtx.expression().map {
                it.accept(this)
                typeTable[it] ?: throw ExpressionHasNoValueException(it)
            }
            initT.getDeclaredConstructor(args) ?: throw ConstructorNotFoundException(initT.name, args, ctx)
        }
        typeTable[ctx] = dotCall(initT, ctx.dotCall())
    }

    override fun visitThisExpression(ctx: ThisExpressionContext) {
        val initT = classT
        ctx.arguments()?.let { argsCtx ->
            val args = argsCtx.expression().map {
                it.accept(this)
                typeTable[it] ?: throw ExpressionHasNoValueException(it)
            }
            initT.getDeclaredConstructor(args) ?: throw ConstructorNotFoundException(initT.name, args, ctx)
        }
        typeTable[ctx] = dotCall(initT, ctx.dotCall())
    }

    override fun visitVariableExpression(ctx: VariableExpressionContext) {
        val initT = ctx.identifier().let {
            scope.find(it.text) ?: throw UnknownVariableException(it.text, it)
        }
        typeTable[ctx] = dotCall(initT, ctx.dotCall())
    }

    override fun visitPrimaryExpression(ctx: PrimaryExpressionContext) {
        typeTable[ctx] = ctx.primary().let {
            it.accept(this)
            typeTable[it] ?: throw ExpressionHasNoValueException(ctx)
        }
    }

    private fun dotCall(initT: VascType, calls: List<DotCallContext>): VascType {
        var nextT: VascType = initT
        for (nextCall in calls) {
            when(nextCall) {
                is FieldAccessContext -> {
                    val name = nextCall.identifier().text
                    nextT = (nextT.getField(name)?.type ?: throw FieldNotFoundException(nextT.name, name, nextCall))
                }
                is MethodCallContext -> {
                    val args = nextCall.arguments().expression().map {
                        it.accept(this)
                        typeTable[it] ?: throw ExpressionHasNoValueException(it)
                    }
                    val name = nextCall.identifier().text
                    val method = nextT.getMethod(name, args) ?: throw MethodNotFoundException(nextT.name, name, args, nextCall)
                    val result = method.returnType
                    if (result == VascVoid && nextCall != calls.last()) {
                        throw MethodReturnsNoValueException(nextT.name, method.name, nextCall)
                    }
                    nextT = result
                }
            }
        }
        return nextT
    }

    private fun copy(
        enclosedScope: Scope = this.scope,
        enclosedReturnT: VascType = this.returnT,
        enclosedClassT: VascType = this.classT
    ) =
        TypeChecker(
            typeResolver = this.typeResolver,
            scope = enclosedScope,
            typeTable = this.typeTable,
            returnT = enclosedReturnT,
            classT = enclosedClassT
        )
}
