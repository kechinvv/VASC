package com.vasc

import com.vasc.antlr.VascLexer
import com.vasc.antlr.VascParser
import com.vasc.checks.constructor.ConstructorCheck
import com.vasc.checks.exhaustiveness.ExhaustivenessCheck
import com.vasc.codegen.CodegenVisitor
import com.vasc.codegen.VascCompiler
import com.vasc.error.VascException
import com.vasc.error.toPrettyString
import com.vasc.type.VascType
import com.vasc.typecheck.TypeCheck
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext

fun main() {
    val src = "src/test/resources/valid/EntryPoint.vas"
    val chars = CharStreams.fromFileName(src)
    val lexer = VascLexer(chars)
    val tokens = CommonTokenStream(lexer)
    val parser = VascParser(tokens)
    val program = parser.program()

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
    val vascProgram = VascCompiler.compile(generator.getGeneratedClasses())
    vascProgram.run("1")
}
