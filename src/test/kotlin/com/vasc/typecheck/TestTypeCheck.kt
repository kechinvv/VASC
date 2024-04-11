package com.vasc.typecheck

import com.vasc.DeclarationCollector
import org.junit.jupiter.api.assertThrows

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import org.antlr.v4.runtime.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File

class TestTypeCheck {
    @TestFactory
    fun `test invalid code`(): Collection<DynamicTest> {
        val dirPath = this::class.java.classLoader.resources("invalid/typecheck").toList().first()
        val dir = File(dirPath.path)
        return dir.listFiles()!!.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                assertThrows<TypeCheckException> {
                    val lexer = VascLexer(CharStreams.fromString(file.readText()))
                    val parser = VascParser(CommonTokenStream(lexer))
                    val program = parser.program()
                    val typeResolver = DeclarationCollector.visitProgram(program)
                    val tc = TypeChecker(typeResolver, Scope(mutableMapOf()), mutableMapOf())
                    tc.visitProgram(program)
                }
            }
        }
    }

    @TestFactory
    fun `test valid code`(): Collection<DynamicTest> {
        val dirPath = this::class.java.classLoader.resources("valid/typecheck").toList().first()
        val dir = File(dirPath.path)
        return dir.listFiles()!!.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                assertDoesNotThrow {
                    val lexer = VascLexer(CharStreams.fromString(file.readText()))
                    val parser = VascParser(CommonTokenStream(lexer))
                    val program = parser.program()
                    val typeResolver = DeclarationCollector.visitProgram(program)
                    val tc = TypeChecker(typeResolver, Scope(mutableMapOf()), mutableMapOf())
                    tc.visitProgram(program)
                }
            }
        }
    }
}
