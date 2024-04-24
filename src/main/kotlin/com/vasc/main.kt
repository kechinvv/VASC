package com.vasc

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import com.vasc.codegen.CodegenVisitor
import com.vasc.codegen.JasminCompiler
import com.vasc.error.VascException
import com.vasc.error.toPrettyString
import com.vasc.exhaustiveness.ExhaustivenessCheck
import com.vasc.type.VascType
import com.vasc.typecheck.TypeCheck
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext

fun main() {
    val src = "src/test/resources/valid/FourBitAdder.vas"
    val chars = CharStreams.fromFileName(src)
    val lexer = VascLexer(chars)
    val tokens = CommonTokenStream(lexer)
    val parser = VascParser(tokens)
    val program = parser.program()

    val errors = mutableListOf<VascException>()
    val typeResolver = makeTypeResolver(program, errors)
    val typeTable: MutableMap<ParserRuleContext, VascType> = mutableMapOf()
    TypeCheck(errors, typeResolver, typeTable = typeTable).check(program)
    ExhaustivenessCheck(typeResolver, typeTable, errors).check(program)
    if (errors.isNotEmpty()) {
        throw IllegalStateException("expected no errors but got:\n" + errors.toPrettyString())
    }
    val generator = CodegenVisitor(typeResolver, typeTable, errors)
    generator.visitProgram(program)
    if (errors.isNotEmpty()) {
        throw IllegalStateException("expected no errors but got:\n" + errors.toPrettyString())
    }
    val compiler = JasminCompiler(generator.getGeneratedClasses())
    val classFiles = compiler.compile()
    compiler.runCompiled(classFiles)
}
