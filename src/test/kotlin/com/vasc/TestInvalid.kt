package com.vasc

import com.vasc.checks.CyclicConstructorException
import com.vasc.checks.DefaultConstructorNotExistException
import com.vasc.checks.NonExhaustiveReturnException
import com.vasc.checks.UnreachableCodeException
import com.vasc.checks.constructor.ConstructorChecker
import com.vasc.checks.exhaustiveness.*
import com.vasc.type.VascType
import com.vasc.typecheck.Scope
import com.vasc.typecheck.TypeChecker
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
                    val tc = TypeChecker(typeResolver, Scope(mutableMapOf()), typeTable)
                    tc.visitProgram(program)
                    assertFailsWith(
                        exceptionClass = exc,
                        block = {
                            ExhaustivenessChecker(typeResolver).visitProgram(program)
                            ConstructorChecker(typeResolver, typeTable).visitProgram(program)
                        })
                }
            }
            tests.addAll(dirTests)
        }
        return tests
    }
}
