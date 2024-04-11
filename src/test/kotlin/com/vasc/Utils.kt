package com.vasc

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import com.vasc.antlr.VascParser.ProgramContext
import org.antlr.v4.runtime.*
import org.junit.jupiter.api.fail
import java.io.File

fun programWithErrorListener(file: File) = programWithErrorListener(CharStreams.fromFileName(file.path))

fun programWithErrorListener(stream: CharStream): ProgramContext {
    val lexer = VascLexer(stream)
    val tokens = CommonTokenStream(lexer)
    val parser = VascParser(tokens)
    val errorListener = VascErrorListener().apply { attachTo(parser) }
    if (errorListener.errors.isNotEmpty()) {
        System.err.println(errorListener.errors.joinToString("\n\n"))
        fail("unexpected parser errors")
    }
    return parser.program()
}

fun testResource(name: String) = File("src/test/resources/$name")