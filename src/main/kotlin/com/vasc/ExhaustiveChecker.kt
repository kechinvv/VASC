package com.vasc

import com.vasc.antlr.VascParser
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.UninitializedLocalVarException
import com.vasc.error.UnreachableCodeException

class ExhaustiveChecker : VascParserBaseVisitor<Unit>() {

    private var waitReturn = true
    private var uninitializedVariables = HashSet<String>()

    override fun visitReturnStatement(ctx: VascParser.ReturnStatementContext) {
        return super.visitReturnStatement(ctx)
    }

    override fun visitBody(ctx: VascParser.BodyContext) {
        val statements = ctx.children.filterIsInstance<VascParser.StatementContext>().toList()
        val n: Int = statements.size
        for (i in 0 until n) {
            if (statements[i].getChild(0) is VascParser.ReturnStatementContext && i != n - 1)
                throw UnreachableCodeException("Unreachable code in line: ${ctx.start.line}")
            statements[i].accept(this)
        }

        if (uninitializedVariables.isNotEmpty())
            throw UninitializedLocalVarException("variables $uninitializedVariables are not initialized (line ${ctx.start.line})")
    }


    override fun visitVariableDeclaration(ctx: VascParser.VariableDeclarationContext) {
        if (ctx.initExpression == null) uninitializedVariables.add(ctx.identifier().text)
    }

    override fun visitIfStatement(ctx: VascParser.IfStatementContext) {
        return super.visitIfStatement(ctx)
    }

    override fun visitAssignStatement(ctx: VascParser.AssignStatementContext?) {
        super.visitAssignStatement(ctx)
    }

    override fun visitMethodDeclaration(ctx: VascParser.MethodDeclarationContext) {
        waitReturn = ctx.returnType != null
        return super.visitMethodDeclaration(ctx)
    }

}

