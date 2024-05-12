package com.vasc.codegen

import java.io.File
import java.io.InputStream

class VascProgram(
    private val target: String,
    private val sourceFile: File,
) {

    fun run(vararg args: Any, outputFile: File? = null) {
        val process = ProcessBuilder(
            "java",
            "-cp",
            sourceFile.absolutePath,
            target,
            *(args.map(Any::toString).toTypedArray())
        ).start().apply {
            inputStream.printLines(outputFile)
            errorStream.printLines(outputFile)
        }
        process.waitFor()
    }

    private fun InputStream.printLines(outputPath: File?) = reader().forEachLine {
        println(it)
        outputPath?.appendText("${it}\n")
    }
}