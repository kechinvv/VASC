package com.vasc.codegen

import jasmin.Main
import java.io.*
import javax.tools.ToolProvider

object VascCompiler {

    fun compile(classes: Map<ClassName, ClassCode>, outputPath: String = "./vascbuild"): VascProgram {
        val jasminDir = newDir("$outputPath/jasmin")
        val classesDir = newDir("$outputPath/classes")

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

    private fun newDir(name: String) = File(name).apply {
        deleteRecursively()
        mkdirs()
    }
}