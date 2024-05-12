package com.vasc.error

import org.antlr.v4.runtime.ParserRuleContext

inline fun <T : ParserRuleContext> tryWithContext(ctx: T, errors: MutableCollection<VascException>, block: (T) -> Unit) {
    try {
        block(ctx)
    } catch (e : VascException) {
        errors.add(
            if (e.ctx != null) e
            else VascException(e.message, ctx)
        )
    }
}