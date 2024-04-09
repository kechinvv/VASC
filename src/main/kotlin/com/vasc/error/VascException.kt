package com.vasc.error

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval

open class VascException(
    description: String,
    ctx: ParserRuleContext? = null
) : RuntimeException(description + prettyPosition(ctx))

private fun prettyPosition(ctx: ParserRuleContext?): String {
    ctx ?: return ""
    val source = ctx.start.inputStream.getText(Interval.of(ctx.start.startIndex, ctx.stop.stopIndex))
    val indent = " ".repeat(ctx.start.charPositionInLine)
    val pointers = "^".repeat(source.length)
    return """
        |
        |at [${ctx.start.line}:${ctx.start.charPositionInLine}]
        |$indent$source
        |$indent$pointers
    """.trimMargin()
}
