package com.vasc

import com.vasc.antlr.VascParser
import com.vasc.antlr.VascParser.ArgumentsContext
import com.vasc.antlr.VascParser.ThisExpressionContext
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.*
import com.vasc.member.VascConstructor
import com.vasc.type.VascType
import com.vasc.util.parentHasDefaultConstructor
import org.antlr.v4.runtime.ParserRuleContext

class ExhaustiveChecker(
    private val typeResolver: VascTypeResolver,
    private val typeTable: MutableMap<ParserRuleContext, VascType>
) : VascParserBaseVisitor<StatementType>() {

    private var waitReturn = true
    private val completeIf = 2
    private var constructorContext: VascConstructor? = null
    private var currentClass: VascType? = null
    private var constructorCalls: MutableMap<VascConstructor, VascConstructor> = mutableMapOf()

    override fun visitClassDeclaration(ctx: VascParser.ClassDeclarationContext): StatementType {
        currentClass = ctx.name.accept(typeResolver)

        if (currentClass!!.declaredConstructors.isEmpty() && !currentClass!!.parentHasDefaultConstructor())
            throw ConstructorsMatchSuperNotExists("No constructors match super (line ${ctx.start.line})")

        constructorCalls = mutableMapOf()
        return super.visitClassDeclaration(ctx)
    }

    override fun visitMethodDeclaration(ctx: VascParser.MethodDeclarationContext): StatementType {
        waitReturn = ctx.returnType != null
        return super.visitMethodDeclaration(ctx)
    }

    override fun visitConstructorDeclaration(ctx: VascParser.ConstructorDeclarationContext): StatementType {
        constructorContext =
            currentClass!!.getDeclaredConstructor(
                ctx.parameters().parameter().map { it.className().accept(typeResolver) })
        val res = super.visitConstructorDeclaration(ctx)
        constructorContext = null
        return res
    }

    override fun visitBody(ctx: VascParser.BodyContext): StatementType {
        var statementType = StatementType.OTHER
        var wasSuperOrThisCall = false

        val statements = ctx.statement().toList()
        val n: Int = statements.size

        val parentIsMethod = ctx.parent is VascParser.MethodDeclarationContext

        for (i in 0 until n) {
            statementType = statements[i].accept(this)
            when (statementType) {
                StatementType.EXHAUSTIVE_RETURN ->
                    if (i != n - 1) throw UnreachableCodeException("Unreachable code (line ${statements[i + 1].start.line})")
                    else if (parentIsMethod) waitReturn = false

                StatementType.THIS_CONSTRUCTOR -> {
                    if (constructorContext == null || i != 0)
                        throw ThisConstructorCallException("This Constructor must be called first in constructor body (line ${ctx.start.line})")
                    wasSuperOrThisCall = true
                }

                StatementType.SUPER -> {
                    if (constructorContext == null || i != 0)
                        throw SuperConstructorCallException("Super Constructor must be called first in constructor body (line ${ctx.start.line})")
                    wasSuperOrThisCall = true
                }

                else -> continue
            }
        }

        if (parentIsMethod && waitReturn)
            throw ExhaustiveReturnException("Exhaustive Return (line ${ctx.start.line})")
        if (constructorContext != null && !wasSuperOrThisCall && !currentClass!!.parentHasDefaultConstructor())
            throw DefaultConstructorNotExistException("Default constructor not exist. Use super call. (line ${ctx.start.line})")
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

    override fun visitThisExpression(ctx: ThisExpressionContext): StatementType {
        super.visitThisExpression(ctx)
        if (ctx.arguments() == null) return StatementType.THIS_CALL
        constructorCalls[constructorContext!!] = getConstructorByArgs(ctx.arguments())
        checkCyclicConstructor(constructorContext!!)
        return StatementType.THIS_CONSTRUCTOR
    }

    override fun defaultResult(): StatementType {
        return StatementType.OTHER
    }

    private fun getConstructorByArgs(args: ArgumentsContext): VascConstructor {
        val argTypes = args.expression().map { typeTable[it]!! }
        return currentClass!!.getDeclaredConstructor(argTypes) ?: throw ConstructorNotFound("Constructor not found")
    }

    private fun checkCyclicConstructor(startPoint: VascConstructor) {
        var pointer = constructorCalls[startPoint]
        while (pointer != null) {
            if (pointer == startPoint)
                throw CyclicConstructorException("Cyclic constructor usage")
            pointer = constructorCalls[pointer]
        }
    }

}

enum class StatementType {
    EXHAUSTIVE_RETURN, NON_EXHAUSTIVE_RETURN, THIS_CONSTRUCTOR, THIS_CALL, SUPER, OTHER
}

