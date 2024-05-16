package com.vasc

import com.vasc.codegen.VascProgram
import com.vasc.util.deleteAndCreateNewFile
import java.io.File

fun main() {
    val src = "src/test/resources/valid/withoutMain/FewClassesWithoutMain.vas"
    val vascProgram = VascProgram.getProgramFromSource(src)
    val output = File("./vascoutput/example_output.txt").deleteAndCreateNewFile()
    vascProgram.run(outputFile = output, target = "Random")
}
