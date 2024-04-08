package com.vasc.typecheck

import com.vasc.error.VascException
import com.vasc.type.VascType
import org.antlr.v4.runtime.ParserRuleContext

class TypeCheckException(expected: VascType, actual: VascType, ctx: ParserRuleContext) :
    VascException("""
        |expected type:
        |  $expected
        |but got:
        |  $actual
        |at [${ctx.start.line}:${ctx.start.charPositionInLine}]
    """.trimMargin())
