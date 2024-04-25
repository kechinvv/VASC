package com.vasc.codegen

import java.io.File
import java.io.InputStream

class VascProgram(
    private val target: String,
    private val sourcePath: File,
) {

    fun run(vararg args: String) {
        ProcessBuilder("java", "-cp", sourcePath.absolutePath, target, *args).start().apply {
            inputStream.printLines()
            errorStream.printLines()
        }
    }

    private fun InputStream.printLines() = reader().forEachLine { println(it) }
}