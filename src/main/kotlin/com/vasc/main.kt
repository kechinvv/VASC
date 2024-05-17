package com.vasc

import com.vasc.codegen.VascProgram
import com.vasc.util.deleteAndCreateNewFile
import java.io.File
import kotlin.Array

fun main(args: Array<String>) {
    val filename = args.getOrNull(0) ?: run {
        System.err.println("File name is expected")
        return
    }

    var i = 1
    val target = args.getOrNull(1)?.takeIf { it.startsWith("--target=") }?.let {
        i++
        return@let it.substringAfter("--target=")
    } ?: "Main"

    val vascProgram = VascProgram.getProgramFromSource(filename)
    val output = File("./vascoutput/example_output.txt").deleteAndCreateNewFile()
    vascProgram.run(
        args = args.drop(i).toTypedArray(),
        outputFile = output,
        target = target
    )
}
