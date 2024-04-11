package com.vasc

import org.antlr.v4.runtime.*

class VascErrorListener : BaseErrorListener() {

    val errors = mutableListOf<String>()

    fun attachTo(parser: Parser, removeListeners: Boolean = true) {
        if (removeListeners) parser.removeErrorListeners()
        parser.addErrorListener(this)
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?,
    ) {
        val detailedMsg = """
                            |parser error at $line:$charPositionInLine
                            |$msg
                        """.trimMargin()
        errors.add(detailedMsg)
    }
}