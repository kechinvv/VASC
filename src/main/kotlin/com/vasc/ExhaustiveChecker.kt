package com.vasc

import com.vasc.antlr.VascParser
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.ExhaustiveReturnException
import com.vasc.error.UnreachableCodeException

class ExhaustiveChecker : VascParserBaseVisitor<Boolean>() {

    private var waitReturn = true
    private var uninitializedVariables: MutableMap<String, Int> = mutableMapOf()


    override fun visitBody(ctx: VascParser.BodyContext): Boolean {
        var wasAbsoluteReturn = false

        val statements = ctx.children.filterIsInstance<VascParser.StatementContext>().toList()
        val n: Int = statements.size

        val parentIsMethod = ctx.parent is VascParser.MethodDeclarationContext

        for (i in 0 until n) {
            wasAbsoluteReturn = statements[i].accept(this)
            if (wasAbsoluteReturn)
                if (i != n - 1) throw UnreachableCodeException("Unreachable code (line ${ctx.start.line})")
                else if (parentIsMethod) waitReturn = false
        }

//        if (uninitializedVariables.isNotEmpty())
//            throw UninitializedLocalVarException("Variables $uninitializedVariables are not initialized (line ${ctx.start.line})")
        if (parentIsMethod && waitReturn)
            throw ExhaustiveReturnException("Exhaustive Return (line ${ctx.start.line})")

        return wasAbsoluteReturn
    }


    override fun visitVariableDeclaration(ctx: VascParser.VariableDeclarationContext): Boolean {
//        if (ctx.initExpression == null) uninitializedVariables[ctx.identifier().text] = 1
        return super.visitVariableDeclaration(ctx)
    }

    override fun visitIfStatement(ctx: VascParser.IfStatementContext): Boolean {
        var returnCount = 0
        ctx.children.forEach {
            val childResult = it.accept(this)
            if (childResult) returnCount++
        }

        return returnCount == 2
    }

    override fun visitAssignStatement(ctx: VascParser.AssignStatementContext?): Boolean {
        return super.visitAssignStatement(ctx)
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

