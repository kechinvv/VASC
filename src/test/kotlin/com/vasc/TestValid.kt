package com.vasc

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import com.vasc.type.VascType
import com.vasc.typecheck.Scope
import com.vasc.typecheck.TypeChecker
import org.antlr.v4.runtime.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.fail
import java.io.File

class TestValid {
    @TestFactory
    fun `test valid code`(): Collection<DynamicTest> {
        val dirPath = this::class.java.classLoader.resources("valid").toList().first()
        val dir = File(dirPath.path)
        return dir.listFiles()!!.filter { it.isFile }.map { file ->
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
                val typeResolver = DeclarationCollector.visitProgram(program)
                val typeTable: MutableMap<ParserRuleContext, VascType> = mutableMapOf()
                val tc = TypeChecker(typeResolver, Scope(mutableMapOf()), typeTable)
                tc.visitProgram(program)
                ExhaustivenessChecker(typeResolver, typeTable).visitProgram(program)
            }
        }
    }
}
