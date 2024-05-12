package com.vasc

import com.vasc.checks.constructor.ConstructorCheck
import com.vasc.checks.exhaustiveness.ExhaustivenessCheck
import com.vasc.codegen.CodegenVisitor
import com.vasc.codegen.VascCompiler
import com.vasc.error.VascException
import com.vasc.error.toPrettyString
import com.vasc.type.VascType
import com.vasc.typecheck.TypeCheck
import com.vasc.util.deleteAndCreateNewFile
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.fail
import java.io.File
import kotlin.test.assertTrue

class TestValid {

    @TestFactory
    fun `test valid code`(): Collection<DynamicTest> {
        val expectedOutputDir = File(this.javaClass.getResource("/valid_output_expected")!!.toURI())
        return testResource("valid").listFiles()!!.filter { it.isFile }.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                val program = programWithErrorListener(file)
                val errors = mutableListOf<VascException>()
                val typeResolver = createTypeResolver(program, errors)
                val typeTable: MutableMap<ParserRuleContext, VascType> = mutableMapOf()
                TypeCheck(errors, typeResolver, typeTable = typeTable).check(program)
                ExhaustivenessCheck(typeResolver, errors).check(program)
                ConstructorCheck(typeResolver, typeTable, errors).check(program)
                if (errors.isNotEmpty()) {
                    fail("expected no errors but got:\n" + errors.toPrettyString())
                }

                val generator = CodegenVisitor(typeResolver, typeTable, errors, file.path)
                generator.visitProgram(program)
                if (errors.isNotEmpty()) {
                    fail("expected no errors but got:\n" + errors.toPrettyString())
                }

                val vascProgram = VascCompiler.compile(generator.getGeneratedClasses())
                val actualRes = File("./vascoutput/${file.nameWithoutExtension}.txt").deleteAndCreateNewFile()
                vascProgram.run(outputFile = actualRes)
                val expectedRes =
                    expectedOutputDir.listFiles()!!.firstOrNull { it.nameWithoutExtension == file.nameWithoutExtension }!!
                assertTrue("File equals") { actualRes.contentEquals(expectedRes)}
            }
        }
    }
}
