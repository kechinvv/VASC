package com.vasc

import com.vasc.antlr.VascParser
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.ExhaustiveReturnException
import com.vasc.error.SuperConstructorCallException
import com.vasc.error.ThisConstructorCallException
import com.vasc.error.UnreachableCodeException

class ExhaustiveChecker : VascParserBaseVisitor<StatementType>() {

    private var waitReturn = true
    private val completeIf = 2
    private var constructorCheck = false

    override fun visitMethodDeclaration(ctx: VascParser.MethodDeclarationContext): StatementType {
        waitReturn = ctx.returnType != null
        return super.visitMethodDeclaration(ctx)
    }

    override fun visitConstructorDeclaration(ctx: VascParser.ConstructorDeclarationContext?): StatementType {
        constructorCheck = true
        val res = super.visitConstructorDeclaration(ctx)
        constructorCheck = false
        return res
    }

    override fun visitBody(ctx: VascParser.BodyContext): StatementType {
        var statementType = StatementType.OTHER

        val statements = ctx.children?.filterIsInstance<VascParser.StatementContext>()?.toList() ?: emptyList()
        val n: Int = statements.size

        val parentIsMethod = ctx.parent is VascParser.MethodDeclarationContext

        for (i in 0 until n) {
            statementType = statements[i].accept(this)
            when (statementType) {
                StatementType.EXHAUSTIVE_RETURN ->
                    if (i != n - 1) throw UnreachableCodeException("Unreachable code (line ${statements[i + 1].start.line})")
                    else if (parentIsMethod) waitReturn = false

                StatementType.THIS_CONSTRUCTOR -> if (!constructorCheck || i != 0)
                    throw ThisConstructorCallException("This Constructor must be called first in constructor body (line ${ctx.start.line})")
                StatementType.SUPER -> if (!constructorCheck || i != 0)
                    throw SuperConstructorCallException("Super Constructor must be called first in constructor body (line ${ctx.start.line})")
                else -> continue
            }
        }

        if (parentIsMethod && waitReturn)
            throw ExhaustiveReturnException("Exhaustive Return (line ${ctx.start.line})")

        return statementType
    }

    override fun visitIfStatement(ctx: VascParser.IfStatementContext): StatementType {
        var returnCount = 0
        ctx.children.forEach {
            val childResult = it.accept(this)
            if (childResult == StatementType.EXHAUSTIVE_RETURN) returnCount++
        }

        return if (returnCount == completeIf) StatementType.EXHAUSTIVE_RETURN
        else StatementType.NON_EXHAUSTIVE_RETURN
    }

    override fun visitReturnStatement(ctx: VascParser.ReturnStatementContext): StatementType {
        super.visitReturnStatement(ctx)
        return StatementType.EXHAUSTIVE_RETURN
    }

    override fun visitSuperExpression(ctx: VascParser.SuperExpressionContext?): StatementType {
        super.visitSuperExpression(ctx)
        return StatementType.SUPER
    }

    override fun visitThisExpression(ctx: VascParser.ThisExpressionContext): StatementType {
        super.visitThisExpression(ctx)
        return if (ctx.arguments() != null) StatementType.THIS_CONSTRUCTOR
        else StatementType.THIS_CALL
    }

    override fun defaultResult(): StatementType {
        return StatementType.OTHER
    }

}

enum class StatementType {
    EXHAUSTIVE_RETURN, NON_EXHAUSTIVE_RETURN, THIS_CONSTRUCTOR, THIS_CALL, SUPER, OTHER
}

