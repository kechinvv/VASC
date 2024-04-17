package com.vasc

import com.vasc.error.VascException
import com.vasc.exhaustiveness.*
import com.vasc.type.VascType
import com.vasc.typecheck.Scope
import com.vasc.typecheck.TypeCheck
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertFailsWith

class TestInvalid {

    @TestFactory
    fun `test invalid code`(): Collection<DynamicTest> {
        val dirs = mapOf(
            testResource("invalid/nonExhaustiveReturn") to NonExhaustiveReturnException::class,
            testResource("invalid/unreachableCode") to UnreachableCodeException::class,
            testResource("invalid/recursiveConstructor") to CyclicConstructorException::class,
            testResource("invalid/defaultConstructor") to DefaultConstructorNotExistException::class
        )
        val tests = mutableListOf<DynamicTest>()

        dirs.forEach { (dir, exc) ->
            val dirTests = dir.listFiles()!!.map { file ->
                val stream = CharStreams.fromFileName(file.path)
                DynamicTest.dynamicTest(file.nameWithoutExtension) {
                    val program = programWithErrorListener(stream)
                    val typeResolver = DeclarationCollector.visitProgram(program)
                    val typeTable: MutableMap<ParserRuleContext, VascType> = mutableMapOf()
                    val errors = mutableListOf<VascException>()
                    val tc = TypeCheck(errors, typeResolver, typeTable = typeTable)
                    tc.visitProgram(program)
                    assertFailsWith(
                        exceptionClass = exc,
                        block = { ExhaustivenessChecker(typeResolver, typeTable).visitProgram(program) })
                }
            }
            tests.addAll(dirTests)
        }
        return tests
    }
}
