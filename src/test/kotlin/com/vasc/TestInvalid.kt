package com.vasc

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import com.vasc.error.ExhaustiveReturnException
import org.antlr.v4.runtime.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import java.io.File

class TestInvalid {
    @TestFactory
    fun `test invalid code`(): Collection<DynamicTest> {
        val dirPath = this::class.java.classLoader.resources("invalid/notExhaustiveReturn").toList().first()
        val dir = File(dirPath.path)
        return dir.listFiles()!!.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                val lexer = VascLexer(CharStreams.fromString(file.readText()))
                val parser = VascParser(CommonTokenStream(lexer))
                val errorListener = object : BaseErrorListener() {
                    val errors = mutableListOf<String>()
                    override fun syntaxError(
                        recognizer: Recognizer<*, *>?,
                        offendingSymbol: Any?,
                        line: Int,
                        charPositionInLine: Int,
                        msg: String?,
                        e: RecognitionException?
                    ) {
                        val detailedMsg = """
                            |parser error at $line:$charPositionInLine
                            |$msg
                        """.trimMargin()
                        errors.add(detailedMsg)
                    }
                }
                parser.removeErrorListeners()
                parser.addErrorListener(errorListener)
                val program = parser.program()
                if (errorListener.errors.isNotEmpty()) {
                    System.err.println(errorListener.errors.joinToString("\n\n"))
                    fail("unexpected parser errors in (${file.name}:1)")
                }
                assertThrows<ExhaustiveReturnException> { ExhaustiveChecker().visitProgram(program) }
            }
        }
    }
}
