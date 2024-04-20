package com.vasc.checks.exhaustiveness

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.checks.NonExhaustiveReturnException
import com.vasc.checks.StatementType
import com.vasc.checks.UnreachableCodeException
import com.vasc.type.VascType

class ExhaustivenessChecker(
    private val typeResolver: VascTypeResolver
) : VascParserBaseVisitor<StatementType>() {

    private var waitReturn = true
    private lateinit var currentClass: VascType

    private val completeIf = 2

    override fun visitClassDeclaration(ctx: ClassDeclarationContext): StatementType {
        currentClass = ctx.name.accept(typeResolver)

        return super.visitClassDeclaration(ctx)
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext): StatementType {
        waitReturn = ctx.returnType != null
        return super.visitMethodDeclaration(ctx)
    }


    override fun visitBody(ctx: BodyContext): StatementType {
        var statementType = StatementType.OTHER

        val statements = ctx.statement().toList()
        val n: Int = statements.size

        val parentIsMethod = ctx.parent is MethodDeclarationContext

        for (i in 0 until n) {
            statementType = statements[i].accept(this)
            when (statementType) {
                StatementType.EXHAUSTIVE_RETURN ->
                    if (i != n - 1) throw UnreachableCodeException("Unreachable code (line ${statements[i + 1].start.line})")
                    else if (parentIsMethod) waitReturn = false

                else -> continue
            }
        }

        if (parentIsMethod && waitReturn)
            throw NonExhaustiveReturnException("Nonexhaustive return (line ${ctx.start.line})")

        return statementType
    }


    override fun visitIfStatement(ctx: IfStatementContext): StatementType {
        var returnCount = 0
        ctx.children.forEach {
            val childResult = it.accept(this)
            if (childResult == StatementType.EXHAUSTIVE_RETURN) returnCount++
        }

        return if (returnCount == completeIf) StatementType.EXHAUSTIVE_RETURN
        else StatementType.NOT_EXHAUSTIVE_RETURN
    }


    override fun visitReturnStatement(ctx: ReturnStatementContext): StatementType {
        super.visitReturnStatement(ctx)
        return StatementType.EXHAUSTIVE_RETURN
    }


    override fun defaultResult(): StatementType {
        return StatementType.OTHER
    }

}

