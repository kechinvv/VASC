package com.vasc.codegen

import jasmin.Main
import java.io.*
import javax.tools.ToolProvider


class JasminCompiler(private val classes: Map<String, String>) {


    fun compile(outputPath: String = "./vascbuild"): File {
        val jasminDir = File("$outputPath/jasmin")
        val classesDir = File("$outputPath/classes")

        jasminDir.deleteRecursively()
        classesDir.deleteRecursively()
        jasminDir.mkdirs()
        classesDir.mkdirs()

        classes.forEach { (name, code) ->
            val file = File("${jasminDir.absolutePath}/$name")
            file.createNewFile()
            file.writeText(code)
            Main.main(arrayOf("-d", classesDir.absolutePath, file.path))
        }

        val javaCompiler = ToolProvider.getSystemJavaCompiler()
        File("src/main/java/com/vasc").listFiles()?.forEach {
            javaCompiler.run(null, null, null, it.path, "-d", classesDir.path)
        }
        return classesDir
    }

    fun runCompiled(sourcePath: File, target: String = "com.vasc.Main", vararg args: String) {
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