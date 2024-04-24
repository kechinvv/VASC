package com.vasc.codegen

import java.io.File
import java.io.InputStream

class VascProgram(
    private val target: String,
    private val sourcePath: File,
    private vararg val args: String
) : Runnable {

    override fun run() {
        ProcessBuilder("java", "-cp", sourcePath.absolutePath, target, *args).start().apply {
            inputStream.printLines()
            errorStream.printLines()
        }
    }

    private fun InputStream.printLines() = reader().forEachLine { println(it) }
}