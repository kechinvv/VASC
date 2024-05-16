package com.vasc.codegen

import com.vasc.checks.constructor.ConstructorCheck
import com.vasc.checks.exhaustiveness.ExhaustivenessCheck
import com.vasc.createTypeResolver
import com.vasc.error.VascException
import com.vasc.error.toPrettyString
import com.vasc.type.VascType
import com.vasc.typecheck.TypeCheck
import com.vasc.util.programWithErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.ParserRuleContext
import java.io.File
import java.io.InputStream

class VascProgram(
    private val sourceFile: File,
) {

    companion object {
        fun getProgramFromSource(src: File): VascProgram {
            return getProgramFromSource(src.path)
        }

        fun getProgramFromSource(src: String): VascProgram {
            val chars = CharStreams.fromFileName(src)
            val program = programWithErrorListener(chars)
            val errors = mutableListOf<VascException>()
            val typeResolver = createTypeResolver(program, errors)
            val typeTable: MutableMap<ParserRuleContext, VascType> = mutableMapOf()
            TypeCheck(errors, typeResolver, typeTable = typeTable).check(program)
            ExhaustivenessCheck(typeResolver, errors).check(program)
            ConstructorCheck(typeResolver, typeTable, errors).check(program)
            if (errors.isNotEmpty()) {
                throw IllegalStateException("expected no errors but got:\n" + errors.toPrettyString())
            }
            val generator = CodegenVisitor(typeResolver, typeTable, errors, src)
            generator.visitProgram(program)
            if (errors.isNotEmpty()) {
                throw IllegalStateException("expected no errors but got:\n" + errors.toPrettyString())
            }
            return VascCompiler.compile(generator.getGeneratedClasses())
        }
    }


    fun run(vararg args: Any, outputFile: File? = null, target: String = "Main", packageName: String = "com.vasc") {
        val process = ProcessBuilder(
            "java",
            "-cp",
            sourceFile.absolutePath,
            "${packageName}.${target}",
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