package com.vasc

import com.vasc.codegen.VascProgram
import com.vasc.util.deleteAndCreateNewFile
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import kotlin.collections.List
import kotlin.test.assertTrue

class TestValid {

    @TestFactory
    fun `test valid code simple`(): Collection<DynamicTest> {
        val expectedOutputDir = File(this.javaClass.getResource("/valid_output_expected")!!.toURI())
        return testResource("valid").listFiles()!!.filter { it.isFile }.map { file ->
            val actualRes = File("./vascoutput/${file.nameWithoutExtension}.txt").deleteAndCreateNewFile()
            val expectedRes =
                expectedOutputDir.listFiles()!!
                    .firstOrNull { it.nameWithoutExtension == file.nameWithoutExtension }!!

            dynamicTest(file.nameWithoutExtension) {
                val vascProgram = assertDoesNotThrow { VascProgram.getProgramFromSource(file) }
                vascProgram.run(outputFile = actualRes)
                assertTrue("File equals") { actualRes.contentEquals(expectedRes) }
            }
        }
    }


    @TestFactory
    fun `test valid code constructors`(): Collection<DynamicNode> {
        val argsForFile = mapOf<String, List<List<Any>>>(
            "EntryPoint.vas" to listOf(listOf(), listOf(1), listOf(1.0), listOf(1, true))
        )
        val expectedOutputDir = File(this.javaClass.getResource("/valid_output_expected")!!.toURI())
        return testResource("valid").listFiles()!!.filter { argsForFile.containsKey(it.name) }.map { file ->

            dynamicContainer(file.name, run {
                argsForFile[file.name]!!.map { args ->
                    val namePostfix = args.joinToString("_") { it.toString().replace('.', 'd') }
                    val actualRes = File(
                        "./vascoutput/${file.nameWithoutExtension}${namePostfix}.txt"
                    ).deleteAndCreateNewFile()
                    val expectedRes =
                        expectedOutputDir.listFiles()!!
                            .firstOrNull { it.nameWithoutExtension == (file.nameWithoutExtension + namePostfix) }!!

                    dynamicTest("${file.nameWithoutExtension} with $args") {
                        val vascProgram = assertDoesNotThrow { VascProgram.getProgramFromSource(file) }
                        println(args)
                        vascProgram.run(*(args.toTypedArray()), outputFile = actualRes)
                        assertTrue("File equals") { actualRes.contentEquals(expectedRes) }
                    }
                }
            })
        }
    }

    @TestFactory
    fun `test valid code without main`(): Collection<DynamicNode> {
        val classesOfFile = mapOf(
            "FewClassesWithoutMain.vas" to listOf("EntryPoint", "Random")
        )
        val expectedOutputDir = File(this.javaClass.getResource("/valid_output_expected")!!.toURI())
        return testResource("valid/withoutMain").listFiles()!!.filter { classesOfFile.containsKey(it.name) }
            .map { file ->
                dynamicContainer(file.name, run {
                    classesOfFile[file.name]!!.map { klass ->
                        val actualRes = File(
                            "./vascoutput/${klass}.txt"
                        ).deleteAndCreateNewFile()
                        val expectedRes =
                            expectedOutputDir.listFiles()!!
                                .firstOrNull { it.nameWithoutExtension == klass }!!

                        dynamicTest("${file.nameWithoutExtension} run $klass") {
                            val vascProgram = assertDoesNotThrow { VascProgram.getProgramFromSource(file) }
                            println(klass)
                            vascProgram.run(outputFile = actualRes, target = klass)
                            assertTrue("File equals") { actualRes.contentEquals(expectedRes) }
                        }
                    }
                })
            }
    }
}
