package com.vasc.typecheck

import com.vasc.*
import org.junit.jupiter.api.*
import java.io.File

class TestTypeCheck {

    @TestFactory
    fun `test invalid code`(): Collection<DynamicTest> {
        return testResource("invalid/typecheck").listFiles()!!.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                assertThrows<TypeCheckException> { test(file) }
            }
        }
    }

    @TestFactory
    fun `test valid code`(): Collection<DynamicTest> {
        return testResource("valid/typecheck").listFiles()!!.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                assertDoesNotThrow { test(file) }
            }
        }
    }

    private fun test(file: File) {
        val program = programWithErrorListener(file)
        val typeResolver = DeclarationCollector.visitProgram(program)
        val tc = TypeChecker(typeResolver, Scope(mutableMapOf()), mutableMapOf())
        tc.visitProgram(program)
    }
}
