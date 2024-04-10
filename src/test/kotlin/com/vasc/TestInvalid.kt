package com.vasc

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import com.vasc.error.ExhaustiveReturnException
import com.vasc.error.CyclicConstructorException
import com.vasc.error.UnreachableCodeException
import com.vasc.type.VascType
import com.vasc.typecheck.Scope
import com.vasc.typecheck.TypeChecker
import org.antlr.v4.runtime.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.fail
import java.io.File
import kotlin.test.assertFailsWith

class TestInvalid {
    @TestFactory
    fun `test invalid code`(): Collection<DynamicTest> {
        val dirs = mapOf(
            File(
                this::class.java.classLoader.resources("invalid/notExhaustiveReturn").toList().first().path
            ) to ExhaustiveReturnException::class,
            File(
                this::class.java.classLoader.resources("invalid/unreachableCode").toList().first().path
            ) to UnreachableCodeException::class,
            File(this::class.java.classLoader.resources("invalid/recursiveConstructor").toList().first().path
            ) to CyclicConstructorException::class
        )
        val tests = mutableListOf<DynamicTest>()

        dirs.forEach { (dir, exc) ->
            val dirTests = dir.listFiles()!!.map { file ->
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
                    assertFailsWith(
                        exceptionClass = exc,
                        block = { ExhaustiveChecker(typeResolver, typeTable).visitProgram(program) })
                }
            }
            tests.addAll(dirTests)
        }
        return tests
    }
}
