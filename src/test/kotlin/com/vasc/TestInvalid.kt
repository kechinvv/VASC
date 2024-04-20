package com.vasc

import com.vasc.checks.CyclicConstructorException
import com.vasc.checks.DefaultConstructorNotExistsException
import com.vasc.checks.NonExhaustiveReturnException
import com.vasc.checks.UnreachableCodeException
import com.vasc.checks.constructor.ConstructorCheck
import com.vasc.checks.exhaustiveness.ExhaustivenessCheck
import com.vasc.error.VascException
import com.vasc.error.toPrettyString
import com.vasc.type.VascType
import com.vasc.typecheck.TypeCheck
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.fail

class TestInvalid {

    @TestFactory
    fun `test invalid code`(): Collection<DynamicTest> {
        val dirs = mapOf(
            testResource("invalid/nonExhaustiveReturn") to NonExhaustiveReturnException::class,
            testResource("invalid/unreachableCode") to UnreachableCodeException::class,
            testResource("invalid/recursiveConstructor") to CyclicConstructorException::class,
            testResource("invalid/defaultConstructor") to DefaultConstructorNotExistsException::class
        )
        val tests = mutableListOf<DynamicTest>()

        dirs.forEach { (dir, exc) ->
            val dirTests = dir.listFiles()!!.map { file ->
                val stream = CharStreams.fromFileName(file.path)
                DynamicTest.dynamicTest(file.nameWithoutExtension) {
                    val program = programWithErrorListener(stream)
                    val errors = mutableListOf<VascException>()
                    val typeResolver = makeTypeResolver(program, errors)
                    val typeTable: MutableMap<ParserRuleContext, VascType> = mutableMapOf()
                    TypeCheck(errors, typeResolver, typeTable = typeTable).check(program)
                    ExhaustivenessCheck(typeResolver, errors).check(program)
                    ConstructorCheck(typeResolver, typeTable, errors).check(program)
                    if (errors.find { it::class == exc } == null) {
                        fail("expected error of type '$exc' but got:\n" + errors.toPrettyString())
                    }
                }
            }
            tests.addAll(dirTests)
        }
        return tests
    }
}
