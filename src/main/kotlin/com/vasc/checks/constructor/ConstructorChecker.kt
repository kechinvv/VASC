package com.vasc.checks.constructor

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.checks.*
import com.vasc.member.VascConstructor
import com.vasc.type.VascType
import org.antlr.v4.runtime.ParserRuleContext

class ConstructorChecker(
    private val typeResolver: VascTypeResolver,
    private val typeTable: MutableMap<ParserRuleContext, VascType>
) : VascParserBaseVisitor<StatementType>() {
    private var currentConstructor: VascConstructor? = null
    private lateinit var currentClass: VascType
    private var constructorCalls: MutableMap<VascConstructor, VascConstructor> = mutableMapOf()

    override fun visitClassDeclaration(ctx: ClassDeclarationContext): StatementType {
        currentClass = ctx.name.accept(typeResolver)

        if (currentClass.declaredConstructors.isEmpty() && !currentClass.parentHasDefaultConstructor())
            throw ConstructorsMatchSuperNotExists("No constructors match super (line ${ctx.start.line})")

        constructorCalls = mutableMapOf()
        return super.visitClassDeclaration(ctx)
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

        ctx.parent is MethodDeclarationContext

        for (i in 0 until n) {
            statementType = statements[i].accept(this)
            when (statementType) {

                StatementType.THIS_CONSTRUCTOR -> {
                    if (currentConstructor == null || i != 0)
                        throw ThisConstructorCallException("This Constructor must be called first in constructor body (line ${ctx.start.line})")
                    wasSuperOrThisCall = true
                }

                StatementType.SUPER -> {
                    if (currentConstructor == null || i != 0)
                        throw SuperConstructorCallException("Super Constructor must be called first in constructor body (line ${ctx.start.line})")
                    wasSuperOrThisCall = true
                }

                else -> continue
            }
        }

        if (currentConstructor != null && !wasSuperOrThisCall && !currentClass.parentHasDefaultConstructor())
            throw DefaultConstructorNotExistException("Default constructor not exist. Use super call. (line ${ctx.start.line})")
        return statementType
    }

    override fun visitThisExpression(ctx: ThisExpressionContext): StatementType {
        super.visitThisExpression(ctx)
        if (ctx.arguments() == null) return StatementType.THIS_CALL
        constructorCalls[currentConstructor!!] = getConstructorByArgs(ctx.arguments())
        checkCyclicConstructor(currentConstructor!!)
        return StatementType.THIS_CONSTRUCTOR
    }

    override fun visitSuperExpression(ctx: SuperExpressionContext?): StatementType {
        super.visitSuperExpression(ctx)
        return StatementType.SUPER
    }

    private fun VascType.parentHasDefaultConstructor(): Boolean {
        return parent == null || parent!!.getDeclaredConstructor(emptyList()) != null || parent!!.declaredConstructors.isEmpty()
    }

    private fun getConstructorByArgs(args: ArgumentsContext): VascConstructor {
        val argTypes = args.expression().map { typeTable[it]!! }
        return currentClass.getDeclaredConstructor(argTypes) ?: throw ConstructorNotFound("Constructor not found")
    }

    private fun checkCyclicConstructor(startPoint: VascConstructor) {
        var pointer = constructorCalls[startPoint]
        while (pointer != null) {
            if (pointer == startPoint)
                throw CyclicConstructorException("Cyclic constructor usage")
            pointer = constructorCalls[pointer]
        }
    }

    override fun defaultResult(): StatementType {
        return StatementType.OTHER
    }
}