package com.vasc.codegen

import jasmin.Main
import java.io.*
import javax.tools.ToolProvider


class Compiler(private val classes: Map<String, String>) {


    fun compile(outputPath: String = "./vascbuild"): VascProgram {
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
        return VascProgram("com.vasc.Main", classesDir)
    }


}