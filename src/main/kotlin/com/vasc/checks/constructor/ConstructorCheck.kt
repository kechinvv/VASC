package com.vasc.checks.constructor

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.checks.*
import com.vasc.check.Check
import com.vasc.error.VascException
import com.vasc.member.VascConstructor
import com.vasc.type.VascType
import org.antlr.v4.runtime.ParserRuleContext

class ConstructorCheck(
    private val typeResolver: VascTypeResolver,
    private val typeTable: MutableMap<ParserRuleContext, VascType>,
    private val errors: MutableList<VascException>
) : VascParserBaseVisitor<StatementType>(), Check {

    private var currentConstructor: VascConstructor? = null
    private lateinit var currentClass: VascType
    private var constructorCalls: MutableMap<VascConstructor, VascConstructor> = mutableMapOf()

    override fun check(program: ProgramContext) {
        visitProgram(program)
    }

    override fun visitClassDeclaration(ctx: ClassDeclarationContext): StatementType {
        currentClass = ctx.name.accept(typeResolver)

        if (currentClass.declaredConstructors.isEmpty() && !currentClass.parentHasDefaultConstructor()) {
            errors.add(ConstructorsMatchSuperNotExists(ctx))
        }

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
                    if (currentConstructor == null || i != 0) {
                        errors.add(IllegalThisConstructorCallException(statements[i]))
                    }
                    wasSuperOrThisCall = true
                }

                StatementType.SUPER_CONSTRUCTOR -> {
                    if (currentConstructor == null || i != 0) {
                        errors.add(IllegalSuperConstructorCallException(statements[i]))
                    }
                    wasSuperOrThisCall = true
                }

                else -> continue
            }
        }

        if (currentConstructor != null && !wasSuperOrThisCall && !currentClass.parentHasDefaultConstructor()) {
            errors.add(DefaultConstructorNotExistsException(ctx.parent as ConstructorDeclarationContext))
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

    override fun visitSuperExpression(ctx: SuperExpressionContext): StatementType {
        super.visitSuperExpression(ctx)
        return if (ctx.arguments() != null)
            StatementType.SUPER_CONSTRUCTOR
        else
            StatementType.OTHER
    }

    private fun VascType.parentHasDefaultConstructor(): Boolean {
        return parent == null || parent!!.getDeclaredConstructor(emptyList()) != null || parent!!.declaredConstructors.isEmpty()
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

    override fun defaultResult(): StatementType {
        return StatementType.OTHER
    }
}