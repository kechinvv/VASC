package com.vasc.typecheck

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.type.*
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
    }

    override fun visitAssignStatement(ctx: AssignStatementContext) {
        val expectedT = currentScope.find(ctx.identifier().text)!!
        ctx.expression().accept(this)
        val actualT = typeTable[ctx]!!
        if (expectedT != actualT) {
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
        val enclosed = copy(currentScope.enclosed(
            fields.associate {
                val varDeclaration = (it as FieldDeclarationContext).variableDeclaration()
                varDeclaration.identifier().text to typeTable[varDeclaration]!!
            }.toMutableMap())
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
        ctx.expression().accept(this)
        val actualT = typeTable[ctx.expression()]!!
        if (expectedT != actualT) {
            throw TypeCheckException(expectedT, actualT, ctx)
        }
    }

    private fun copy(enclosedScope: Scope) = TypeChecker(this.typeResolver, enclosedScope, this.typeTable)
}
