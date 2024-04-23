package com.vasc

import com.vasc.antlr.VascParser
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
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.fail
import java.io.File
import kotlin.reflect.KClass


class TestInvalidChecks {

    @TestFactory
    fun `Test ExhaustivenessCheck for invalid code`(): Collection<DynamicNode> {
        val dirs = mapOf(
            testResource("invalid/nonExhaustiveReturn") to NonExhaustiveReturnException::class,
            testResource("invalid/unreachableCode") to UnreachableCodeException::class
        )
        val runChecks: RunnableChecks =
            { typeResolver, _, errors, program -> ExhaustivenessCheck(typeResolver, errors).check(program) }

        return dirs.map { (dir, exc) ->
            dynamicContainer("Test check for ${exc.simpleName}",
                dir.listFiles()!!.map { getDynamicTestForFile(it, exc, runChecks) })
        }
    }

    @TestFactory
    fun `Test ConstructorCheck for invalid code`(): Collection<DynamicNode> {
        val dirs = mapOf(
            testResource("invalid/recursiveConstructor") to CyclicConstructorException::class,
            testResource("invalid/defaultConstructor") to DefaultConstructorNotExistsException::class
        )
        val runChecks: RunnableChecks = { typeResolver, typeTable, errors, program ->
            ConstructorCheck(typeResolver, typeTable, errors).check(program)
        }

        return dirs.map { (dir, exc) ->
            dynamicContainer("Test check for ${exc.simpleName}",
                dir.listFiles()!!.map { getDynamicTestForFile(it, exc, runChecks) })
        }
    }

    private fun getDynamicTestForFile(
        file: File, exc: KClass<out VascException>, enabledChecks: RunnableChecks
    ): DynamicTest {
        val stream = CharStreams.fromFileName(file.path)
        return dynamicTest(file.nameWithoutExtension) {
            val program = programWithErrorListener(stream)
            val errors = mutableListOf<VascException>()
            val typeResolver = createTypeResolver(program, errors)
            val typeTable: MutableMap<ParserRuleContext, VascType> = mutableMapOf()
            TypeCheck(errors, typeResolver, typeTable = typeTable).check(program)

            enabledChecks(typeResolver, typeTable, errors, program)

            if (errors.find { it::class == exc } == null) {
                fail("expected error of type '$exc' but got:\n" + errors.toPrettyString())
            }
        }
    }


}

typealias RunnableChecks = (
    VascTypeResolver, MutableMap<ParserRuleContext, VascType>, MutableList<VascException>, VascParser.ProgramContext
) -> Unit
