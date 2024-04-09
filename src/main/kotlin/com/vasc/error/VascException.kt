package com.vasc.error

import org.antlr.v4.runtime.ParserRuleContext

open class VascException(
    private val decsription: String,
    private val ctx: ParserRuleContext? = null
) : RuntimeException(decsription + (ctx?.let { "\nat [${ctx.start.line}:${ctx.start.charPositionInLine}]" } ?: ""))