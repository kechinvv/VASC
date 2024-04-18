package com.vasc

import com.vasc.error.VascException
import com.vasc.exhaustiveness.ExhaustivenessCheck
import com.vasc.type.VascType
import com.vasc.typecheck.TypeCheck
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class TestValid {

    @TestFactory
    fun `test valid code`(): Collection<DynamicTest> {
        return testResource("valid").listFiles()!!.filter { it.isFile }.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                val program = programWithErrorListener(file)
                val errors = mutableListOf<VascException>()
                val typeResolver = makeTypeResolver(program, errors)
                val typeTable: MutableMap<ParserRuleContext, VascType> = mutableMapOf()
                val tc = TypeCheck(errors, typeResolver, typeTable = typeTable)
                tc.visitProgram(program)
                ExhaustivenessCheck(typeResolver, typeTable, errors).visitProgram(program)
            }
        }
    }
}
