package com.vasc.codegen

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class VascProgram(private val target: String, private val sourcePath: File) {

    fun run(vararg args: String) {
        val process = ProcessBuilder("java", "-cp", sourcePath.absolutePath, target, *args).start()
        val r = BufferedReader(InputStreamReader(process.inputStream))
        val errors = BufferedReader(InputStreamReader(process.errorStream))
        Thread {
            try {
                printer(r)
                printer(errors)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }.start()
    }


    private fun printer(reader: BufferedReader) {
        var line = reader.readLine()
        while (line != null) {
            println(line)
            line = reader.readLine()
        }
    }
}