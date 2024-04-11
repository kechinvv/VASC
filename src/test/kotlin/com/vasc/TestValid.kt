package com.vasc

import com.vasc.exhaustiveness.ExhaustivenessChecker
import com.vasc.type.VascType
import com.vasc.typecheck.Scope
import com.vasc.typecheck.TypeChecker
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class TestValid {

    @TestFactory
    fun `test valid code`(): Collection<DynamicTest> {
        return testResource("valid").listFiles()!!.filter { it.isFile }.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                val program = programWithErrorListener(file)
                val typeResolver = DeclarationCollector.visitProgram(program)
                val typeTable: MutableMap<ParserRuleContext, VascType> = mutableMapOf()
                val tc = TypeChecker(typeResolver, Scope(mutableMapOf()), typeTable)
                tc.visitProgram(program)
                ExhaustivenessChecker(typeResolver, typeTable).visitProgram(program)
            }
        }
    }
}
