package com.vasc

import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.member.*
import com.vasc.type.VascClass
import com.vasc.type.VascType
import com.vasc.util.toVascVariable

class DeclarationCollector : VascParserBaseVisitor<Any>() {

    private val declaredClassNames = mutableSetOf("Integer", "Boolean", "Real", "List", "Array")

    private val typeMap = mutableMapOf<String, VascType>()
    private val typeResolver = VascTypeResolver {
        typeMap[it] ?: throw VascException()
    }

    override fun visitProgram(ctx: ProgramContext): VascTypeResolver {
        ctx.classDeclarations.map {
            val name = it.identifier().first().text
            if (!declaredClassNames.add(name)) throw VascException()
            val klass = MutableVascClass(name)
            typeMap[name] = klass
            it to klass
        }.forEach { (decl, klass) ->
            decl.accept(ClassBuilder(klass, typeResolver))
        }

        return typeResolver
    }
}

private class ClassBuilder(
    val vascClass: MutableVascClass,
    val typeResolver: VascTypeResolver,
) : VascParserBaseVisitor<Unit>() {

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        vascClass.parent = ctx.parentClass?.accept(typeResolver)
        ctx.classBody().memberDeclarations.forEach {
            it.accept(this)
        }
    }

    override fun visitFieldDeclaration(ctx: FieldDeclarationContext) {
        val variable = ctx.variableDeclaration().toVascVariable(typeResolver)
        vascClass.addField(variable)
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) {
        val parameters = ctx.parameters().parameter().map { it.toVascVariable(typeResolver) }
        vascClass.addConstructor(VascConstructor(parameters))
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        val method = VascMethod(
            name = ctx.identifier().text,
            parameters = ctx.parameters().parameter().map { it.toVascVariable(typeResolver) },
            returnType = ctx.className().accept(typeResolver)
        )
        vascClass.addMethod(method)
    }
}

private class MutableVascClass(name: String) : VascClass(name) {

    override var parent: VascType? = null
    override val fields = LinkedHashSet<VascVariable>()
    override val methods = LinkedHashSet<VascMethod>()
    override val constructors = LinkedHashSet<VascConstructor>()

    fun addField(field: VascVariable) {
        if (getField(field.name) != null) throw VascException()
        else fields += field
    }

    fun addConstructor(constructor: VascConstructor) {
        if (constructors.contains(constructor)) throw VascException()
        else constructors += constructor
    }

    fun addMethod(method: VascMethod) {
        if (methods.contains(method)) throw VascException()
        else methods += method
    }
}