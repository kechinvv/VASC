package com.vasc

import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.type.*

class VascTypeResolver(
    private val userTypeProvider: (String) -> VascType
) : VascParserBaseVisitor<VascType>() {

    override fun visitArrayType(ctx: ArrayTypeContext): VascType {
        val genericType = ctx.genericType().className().accept(this)
        return VascArray(genericType)
    }

    override fun visitListType(ctx: ListTypeContext): VascType {
        val genericType = ctx.genericType().className().accept(this)
        return VascList(genericType)
    }

    override fun visitIntegerType(ctx: IntegerTypeContext): VascType {
        return VascInteger
    }

    override fun visitBooleanType(ctx: BooleanTypeContext): VascType {
        return VascBoolean
    }

    override fun visitRealType(ctx: RealTypeContext): VascType {
        return VascReal
    }

    override fun visitUserType(ctx: UserTypeContext): VascType {
        return ctx.identifier().accept(this)
    }

    override fun visitIdentifier(ctx: IdentifierContext): VascType {
        return userTypeProvider(ctx.text)
    }
}