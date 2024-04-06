package com.vasc

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main() {
    val src = "src/test/resources/valid/FourBitAdder.vas"
    val chars = CharStreams.fromFileName(src)
    val lexer = VascLexer(chars)
    val tokens = CommonTokenStream(lexer)
    val parser = VascParser(tokens)
    val program = parser.program()

    val typeResolver = DeclarationCollector().visitProgram(program)
}
