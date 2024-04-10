package com.vasc.error

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval

open class VascException(
    description: String, ctx: ParserRuleContext? = null
) : RuntimeException(description + prettyPosition(ctx))

class SuperConstructorCallException(override val message: String) : VascException(message)
class ThisConstructorCallException(override val message: String) : VascException(message)
class ExhaustiveReturnException(override val message: String) : VascException(message)
class UnreachableCodeException(override val message: String) : VascException(message)
class ConstructorNotFound(override val message: String) : VascException(message)
class CyclicConstructorException(override val message: String) : VascException(message)

private fun prettyPosition(ctx: ParserRuleContext?): String {
    ctx ?: return ""
    return when (val parentCtx = ctx.parent) {
        is ParserRuleContext -> prettyPositionWithinParent(ctx, parentCtx)
        else -> prettyPositionNoParent(ctx)
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
