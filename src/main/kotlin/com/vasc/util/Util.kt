package com.vasc.util

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import com.vasc.antlr.VascParser.ProgramContext
import com.vasc.error.VascException
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

class UnexpectedParserError :
    VascException("unexpected parser errors")

fun programWithErrorListener(stream: CharStream): ProgramContext {
    val lexer = VascLexer(stream)
    val tokens = CommonTokenStream(lexer)
    val parser = VascParser(tokens)
    val errorListener = VascErrorListener().apply { attachTo(parser) }
    val program = parser.program()
    if (errorListener.errors.isNotEmpty()) {
        System.err.println(errorListener.errors.joinToString("\n\n"))
        throw UnexpectedParserError()
    }
    return program
}


fun File.deleteAndCreateNewFile(): File {
    return this.apply {
        if (exists()) delete()
        getParentFile().mkdirs()
        createNewFile()
    }
}

