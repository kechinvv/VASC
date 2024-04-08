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

    override fun visitWhileStatement(ctx: WhileStatementContext) {
        val expectedT = VascBoolean
        ctx.expression().accept(this)
        val actualT = typeTable[ctx]!!
        if (expectedT != actualT) {
            throw TypeCheckException(expectedT, actualT, ctx)
        }
        ctx.body().accept(copy(currentScope.enclosed()))
    }

    override fun visitAssignStatement(ctx: AssignStatementContext) {
        val expectedT = currentScope.find(ctx.identifier().text)!!
        ctx.expression().accept(this)
        val actualT = typeTable[ctx]!!
        if (expectedT.isAssignableFrom(actualT)) {
            throw TypeCheckException(expectedT, actualT, ctx)
        }
    }

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        val classT = typeResolver.visit(ctx.name)!!
        val bodyCtx = ctx.classBody()
        val (fields, nonFields) = bodyCtx.memberDeclarations.partition { it is FieldDeclarationContext }
        fields.forEach {
            it.accept(this)
        }
        val enclosed = copy(
            currentScope.enclosed(
                vars = fields.associate {
                    val varDeclaration = (it as FieldDeclarationContext).variableDeclaration()
                    varDeclaration.identifier().text to typeTable[varDeclaration]!!
                }.toMutableMap(),
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
            if (expectedT.isAssignableFrom(actualT)) {
                throw TypeCheckException(expectedT, actualT, ctx)
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

    override fun visitVariableStatement(ctx: VariableStatementContext) {
        val v = ctx.variableDeclaration()
        v.accept(this)
        currentScope.add(v.identifier().text, typeTable[v.expression()]!!)
    }

    override fun visitReturnStatement(ctx: ReturnStatementContext) {
        val expectedT = currentScope.returnT()!!
        val expr = ctx.expression()
        expr.accept(this)
        val actualT = typeTable[expr]!!
        if (expectedT.isAssignableFrom(actualT)) {
            throw TypeCheckException(actualT, expectedT, ctx)
        }
    }

    override fun visitCallableExpression(ctx: CallableExpressionContext) {
        ctx.arguments().expression().forEach { it.accept(this) }
        val name = ctx.className().text
        val params = ctx.arguments().expression().map { typeTable[it]!! }
        val initT = currentScope.classT()?.getMethod(name, params)?.returnType ?: typeResolver.visit(ctx.className())!!
        typeTable[ctx] = dotCall(initT, ctx.dotCall())
    }

    private fun dotCall(initT: VascType, calls: List<DotCallContext>): VascType {
        var nextT = initT
        for (nextCall in calls) {
            when(nextCall) {
                is FieldAccessContext -> {
                    nextT = nextT.getField(nextCall.identifier().text)!!.type
                }
                is MethodCallContext -> {
                    val args = nextCall.arguments().expression().map { typeTable[it]!! }
                    nextT = nextT.getMethod(nextCall.identifier().text, args)!!.returnType!!
                }
            }
        }
        return nextT
    }

    override fun visitIfStatement(ctx: IfStatementContext) {
        val expectT = VascBoolean
        val expr = ctx.expression()
        expr.accept(this)
        val actualT = typeTable[expr]!!
        if (expectT != actualT) {
            throw TypeCheckException(expectT, actualT, ctx)
        }
        ctx.thenBody.accept(copy(currentScope.enclosed()))
        ctx.elseBody.accept(copy(currentScope.enclosed()))
    }

    override fun visitSuperExpression(ctx: SuperExpressionContext) {
        val initT = currentScope.classT()!!.parent!!
        // TODO: check constructor
        typeTable[ctx] = dotCall(initT, ctx.dotCall())
    }

    override fun visitThisExpression(ctx: ThisExpressionContext) {
        val initT = currentScope.classT()!!
        typeTable[ctx] = dotCall(initT, ctx.dotCall())
    }

    override fun visitVariableExpression(ctx: VariableExpressionContext) {
        val initT = currentScope.find(ctx.identifier().text)!!
        typeTable[ctx] = dotCall(initT, ctx.dotCall())
    }

    override fun visitPrimaryExpression(ctx: PrimaryExpressionContext) {
        ctx.primary().accept(this)
        typeTable[ctx] = typeTable[ctx.primary()]!!
    }

    private fun copy(enclosedScope: Scope) = TypeChecker(this.typeResolver, enclosedScope, this.typeTable)
}
