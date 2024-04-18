package com.vasc.exhaustiveness

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.member.VascConstructor
import com.vasc.type.VascType
import org.antlr.v4.runtime.ParserRuleContext

class ExhaustivenessChecker(
    private val typeResolver: VascTypeResolver,
    private val typeTable: MutableMap<ParserRuleContext, VascType>,
    private val errors: MutableList<VascException>,
) : VascParserBaseVisitor<ExhaustivenessChecker.StatementType>() {

    private var waitReturn = true
    private var currentConstructor: VascConstructor? = null
    private lateinit var currentClass: VascType
    private var constructorCalls: MutableMap<VascConstructor, VascConstructor> = mutableMapOf()

    private val completeIf = 2

    override fun visitClassDeclaration(ctx: ClassDeclarationContext): StatementType {
        currentClass = ctx.name.accept(typeResolver)

        if (currentClass.declaredConstructors.isEmpty() && !currentClass.parentHasDefaultConstructor()) {
            errors.add(ConstructorsMatchSuperNotExists(ctx))
        }

        constructorCalls = mutableMapOf()
        return super.visitClassDeclaration(ctx)
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext): StatementType {
        waitReturn = ctx.returnType != null
        return super.visitMethodDeclaration(ctx)
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext): StatementType {
        currentConstructor =
            currentClass.getDeclaredConstructor(
                ctx.parameters().parameter().map { it.className().accept(typeResolver) })
        val res = super.visitConstructorDeclaration(ctx)
        currentConstructor = null
        return res
    }

    override fun visitBody(ctx: BodyContext): StatementType {
        var statementType = StatementType.OTHER
        var wasSuperOrThisCall = false

        val statements = ctx.statement().toList()
        val n: Int = statements.size

        val parentIsMethod = ctx.parent is MethodDeclarationContext

        for (i in 0 until n) {
            statementType = statements[i].accept(this)
            when (statementType) {
                StatementType.EXHAUSTIVE_RETURN ->
                    if (i != n - 1) {
                        errors.add(UnreachableCodeException(statements[i + 1]))
                    }
                    else if (parentIsMethod) waitReturn = false

                StatementType.THIS_CONSTRUCTOR -> {
                    if (currentConstructor == null || i != 0) {
                        errors.add(ThisConstructorCallException(statements[i]))
                    }
                    wasSuperOrThisCall = true
                }

                StatementType.SUPER -> {
                    if (currentConstructor == null || i != 0) {
                        errors.add(SuperConstructorCallException(statements[i]))
                    }
                    wasSuperOrThisCall = true
                }

                else -> continue
            }
        }

        if (parentIsMethod && waitReturn) {
            errors.add(NonExhaustiveReturnException(ctx))
        }
        if (currentConstructor != null && !wasSuperOrThisCall && !currentClass.parentHasDefaultConstructor()) {
            errors.add(DefaultConstructorNotExistException(ctx))
        }
        return statementType
    }

    override fun visitThisExpression(ctx: ThisExpressionContext): StatementType {
        super.visitThisExpression(ctx)
        if (ctx.arguments() == null) return StatementType.THIS_CALL
        getConstructorByArgs(ctx)?.let {
            constructorCalls[currentConstructor!!] = it
        }
        checkCyclicConstructor(currentConstructor!!, ctx)
        return StatementType.THIS_CONSTRUCTOR
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

    override fun visitSuperExpression(ctx: SuperExpressionContext?): StatementType {
        super.visitSuperExpression(ctx)
        return StatementType.SUPER
    }

    override fun defaultResult(): StatementType {
        return StatementType.OTHER
    }

    private fun getConstructorByArgs(ctx: ThisExpressionContext): VascConstructor? {
        val argTypes = ctx.arguments().expression().map { typeTable[it]!! }
        val constructor = currentClass.getDeclaredConstructor(argTypes)
        if (constructor == null) {
            errors.add(ConstructorNotFound(ctx))
        }
        return constructor
    }

    private fun checkCyclicConstructor(startPoint: VascConstructor, ctx: ThisExpressionContext) {
        var pointer = constructorCalls[startPoint]
        while (pointer != null) {
            if (pointer == startPoint) {
                errors.add(CyclicConstructorException(ctx))
                break
            } else {
                pointer = constructorCalls[pointer]
            }
        }
    }

    private fun VascType.parentHasDefaultConstructor(): Boolean {
        return parent == null || parent!!.getDeclaredConstructor(emptyList()) != null || parent!!.declaredConstructors.isEmpty()
    }

    enum class StatementType {
        EXHAUSTIVE_RETURN, NOT_EXHAUSTIVE_RETURN, THIS_CONSTRUCTOR, THIS_CALL, SUPER, OTHER
    }
}

