package com.vasc.error

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval

open class VascException(
    final override val message: String, ctx: ParserRuleContext? = null
) : RuntimeException(message) {

    val fullMessage = message + (ctx?.let { prettyPosition(it) } ?: "")

    private fun prettyPosition(ctx: ParserRuleContext): String {
        return try { // getSourceText can fail (for some reason)
            when (val parentCtx = ctx.parent) {
                is ParserRuleContext -> prettyPositionWithinParent(ctx, parentCtx)
                else -> prettyPositionNoParent(ctx)
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun prettyPositionNoParent(ctx: ParserRuleContext): String {
        val source = getSourceText(ctx)
        val indent = " ".repeat(ctx.start.charPositionInLine)
        val pointers = "^".repeat(source.lines().maxOfOrNull { it.length }!!)
        return """
        |
        |${at(ctx)}
        |$indent$source
        |$indent$pointers
    """.trimMargin()
    }

    private fun prettyPositionWithinParent(ctx: ParserRuleContext, parentCtx: ParserRuleContext): String {
        val source = getSourceText(ctx)
        val parentSource = getSourceText(parentCtx)
        val indent = " ".repeat(ctx.start.charPositionInLine)
        val indentParent = " ".repeat(parentCtx.start.charPositionInLine)
        val (before, after) = parentSource.lines().mapIndexed { n, line -> Pair(n, line) }
            .partition { it.first + parentCtx.start.line <= ctx.start.line }
        val pointers = "^".repeat(source.lines().last().length)
        return """
            |
            |${at(ctx)}
            |$indentParent${before.joinToString("\n") { it.second }}  
            |$indent$pointers
            |${after.joinToString("\n") { it.second }}
        """.trimMargin()
    }

    private fun getSourceText(ctx: ParserRuleContext) =
        ctx.start.inputStream.getText(Interval.of(ctx.start.startIndex, ctx.stop.stopIndex))

    private fun at(ctx: ParserRuleContext) = "at [${ctx.start.line}:${ctx.start.charPositionInLine}]"
}

fun List<VascException>.toPrettyString() = mapIndexed { i, e -> "## ERROR ${i + 1} ##\n ${e.fullMessage}\n" }.joinToString("")
