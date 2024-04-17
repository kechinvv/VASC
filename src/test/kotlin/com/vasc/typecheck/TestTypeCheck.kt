package com.vasc.typecheck

import com.vasc.*
import com.vasc.error.VascException
import org.junit.jupiter.api.*
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestTypeCheck {

    @TestFactory
    fun `test invalid code`(): Collection<DynamicTest> {
        return testResource("invalid/typecheck").listFiles()!!.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                assertDoesNotThrow {
                    test(file, false)
                }
            }
        }
    }

    @TestFactory
    fun `test valid code`(): Collection<DynamicTest> {
        return testResource("valid/typecheck").listFiles()!!.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                assertDoesNotThrow {
                    test(file, true)
                }
            }
        }
    }

    private fun test(file: File, valid: Boolean) {
        val program = programWithErrorListener(file)
        val typeResolver = DeclarationCollector.visitProgram(program)
        val errors = mutableListOf<VascException>()
        val tc = TypeCheck(errors, typeResolver)
        tc.check(program)
        if (valid) {
            if (errors.isNotEmpty()) {
                val msg = "expected no errors but got:\n" + errors.mapIndexed { i, e -> "## ERROR ${i + 1} ##\n ${e.message}\n" }.joinToString("")
                fail(msg)
            }
        } else {
            if (errors.isEmpty()) {
                val msg = "expected error but got nothing\n"
                fail(msg)
            }
        }
    }
}
