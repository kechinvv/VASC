package com.vasc

import com.vasc.antlr.VascParser
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.ExhaustiveReturnException
import com.vasc.error.UnreachableCodeException

class ExhaustiveChecker : VascParserBaseVisitor<Boolean>() {

    private var waitReturn = true
    private val completeIf = 2


    override fun visitBody(ctx: VascParser.BodyContext): Boolean {
        var wasAbsoluteReturn = false

        val statements = ctx.children?.filterIsInstance<VascParser.StatementContext>()?.toList() ?: emptyList()
        val n: Int = statements.size

        val parentIsMethod = ctx.parent is VascParser.MethodDeclarationContext

        for (i in 0 until n) {
            wasAbsoluteReturn = statements[i].accept(this)
            if (wasAbsoluteReturn)
                if (i != n - 1) throw UnreachableCodeException("Unreachable code (line ${statements[i + 1].start.line})")
                else if (parentIsMethod) waitReturn = false
        }

        if (parentIsMethod && waitReturn)
            throw ExhaustiveReturnException("Exhaustive Return (line ${ctx.start.line})")

        return wasAbsoluteReturn
    }

    override fun visitIfStatement(ctx: VascParser.IfStatementContext): Boolean {
        var returnCount = 0
        ctx.children.forEach {
            val childResult = it.accept(this)
            if (childResult) returnCount++
        }

        return returnCount == completeIf
    }

    override fun visitMethodDeclaration(ctx: VascParser.MethodDeclarationContext): Boolean {
        waitReturn = ctx.returnType != null
        return super.visitMethodDeclaration(ctx)
    }

    override fun visitReturnStatement(ctx: VascParser.ReturnStatementContext): Boolean {
        super.visitReturnStatement(ctx)
        return true
    }

    override fun defaultResult(): Boolean {
        return false
    }

}

