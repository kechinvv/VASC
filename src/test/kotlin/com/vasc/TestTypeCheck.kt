package com.vasc

import com.vasc.typecheck.TypeCheckException
import org.junit.jupiter.api.assertThrows

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import com.vasc.typecheck.TypeChecker
import org.antlr.v4.runtime.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File

class TestTypeCheck {
    @TestFactory
    fun `test valid code`(): Collection<DynamicTest> {
        val dirPath = this::class.java.classLoader.resources("invalid/typecheck").toList().first()
        val dir = File(dirPath.path)
        return dir.listFiles()!!.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                val lexer = VascLexer(CharStreams.fromString(file.readText()))
                val parser = VascParser(CommonTokenStream(lexer))
                val program = parser.program()
                val typeResolver = DeclarationCollector.visitProgram(program)
                val tc = TypeChecker(typeResolver)
                assertThrows<TypeCheckException> {
                    tc.visitProgram(program)
                }
            }
        }
    }
}
