package com.vasc

import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.type.*

class VascTypeResolver(
    private val userTypeProvider: (String) -> VascType
) : VascParserBaseVisitor<VascType>() {

    override fun visitBuiltInType(ctx: BuiltInTypeContext): VascType {
        ctx.arrayType()?.let { return it.accept(this@VascTypeResolver) }
        ctx.listType()?.let { return it.accept(this@VascTypeResolver) }

        ctx.BOOL()?.let { return VascBoolean }
        ctx.INT()?.let { return VascInteger }
        ctx.REAL()?.let { return VascReal }

        throw VascException()
    }

    override fun visitIdentifier(ctx: IdentifierContext): VascType {
        return userTypeProvider(ctx.text)
    }

    override fun visitListType(ctx: ListTypeContext): VascType {
        val genericType = ctx.genericType().className().accept(this)
        return VascList(genericType)
    }

    override fun visitArrayType(ctx: ArrayTypeContext): VascType {
        val genericType = ctx.genericType().className().accept(this)
        return VascArray(genericType)
    }

    // TODO: primitives?
}