package com.vasc

import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.member.*
import com.vasc.type.VascClass
import com.vasc.type.VascType
import com.vasc.util.toUniqueVariables
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
        val constructor = VascConstructor(
            parameters = ctx.parameters().toUniqueVariables(typeResolver)
        )
        vascClass.addConstructor(constructor)
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        val method = VascMethod(
            name = ctx.identifier().text,
            parameters = ctx.parameters().toUniqueVariables(typeResolver),
            returnType = ctx.returnType?.accept(typeResolver)
        )
        vascClass.addMethod(method)
    }
}

private class MutableVascClass(name: String) : VascClass(name) {

    override var parent: VascType? = null

    override var declaredFields: MutableList<VascVariable> = mutableListOf()
    override var declaredConstructors: MutableList<VascConstructor> = mutableListOf()
    override var declaredMethods: MutableList<VascMethod> = mutableListOf()

    fun addField(variable: VascVariable) {
        if (getDeclaredField(variable.name) != null) throw VascException()
        declaredFields += variable
    }

    fun addConstructor(constructor: VascConstructor) {
        if (getDeclaredConstructor(constructor.parameterTypes) != null) throw VascException()
        declaredConstructors += constructor
    }

    fun addMethod(method: VascMethod) {
        if (getDeclaredMethod(method.name, method.parameterTypes) != null) throw VascException()
        declaredMethods += method
    }
}