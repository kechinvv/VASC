package com.vasc

import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.member.*
import com.vasc.type.*
import com.vasc.util.toUniqueVariables
import com.vasc.util.toVascVariable

fun makeTypeResolver(ctx: ProgramContext, errors: MutableList<VascException>): VascTypeResolver {
    val namedDeclarations = mutableMapOf<String, ClassDeclarationContext>()
    ctx.classDeclarations.forEach {
        val name = it.name.text
        if (namedDeclarations.contains(name)) {
            errors.add(VascException("Class $name is already declared in this scope", it.name))
        } else {
            namedDeclarations[name] = it
        }
    }

    val classMap = namedDeclarations.mapValues { (name, _) -> MutableVascClass(name) }
    val typeResolver = VascTypeResolver {
        classMap[it] ?: throw VascException("Unknown type: $it")
    }

    namedDeclarations.forEach { (name, decl) ->
        decl.accept(ClassBuilder(classMap[name]!!, typeResolver, errors))
    }

    return typeResolver
}

private class ClassBuilder(
    val vascClass: MutableVascClass,
    val typeResolver: VascTypeResolver,
    val errors: MutableList<VascException>,
) : VascParserBaseVisitor<Unit>() {

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        vascClass.parent = ctx.parentName?.accept(typeResolver)
        ctx.classBody().memberDeclarations.forEach {
            it.accept(this)
        }
    }

    override fun visitFieldDeclaration(ctx: FieldDeclarationContext) {
        try {
            val variable = ctx.variableDeclaration().toVascVariable(typeResolver)
            vascClass.addField(variable)
        } catch (e: VascException) {
            errors.add(VascException(e.description, ctx))
        }
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) {
        try {
            val constructor = VascConstructor(
                parameters = ctx.parameters().toUniqueVariables(typeResolver)
            )
            vascClass.addConstructor(constructor)
        } catch (e: VascException) {
            errors.add(VascException(e.description, ctx))
        }
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        try {
            val method = VascMethod(
                name = ctx.identifier().text,
                parameters = ctx.parameters().toUniqueVariables(typeResolver),
                returnType = ctx.returnType?.accept(typeResolver) ?: VascVoid
            )
            vascClass.addMethod(method)
        } catch (e: VascException) {
            errors.add(VascException(e.description, ctx))
        }
    }
}

private class MutableVascClass(name: String) : VascClass(name) {

    override var parent: VascType? = null

    override var declaredFields: MutableList<VascVariable> = mutableListOf()
    override var declaredConstructors: MutableList<VascConstructor> = mutableListOf()
    override var declaredMethods: MutableList<VascMethod> = mutableListOf()

    fun addField(variable: VascVariable) {
        if (getDeclaredField(variable.name) != null) {
            throw VascException("Duplicate field: $name.${variable.name}")
        }
        declaredFields += variable
    }

    fun addConstructor(constructor: VascConstructor) {
        if (getDeclaredConstructor(constructor.parameterTypes) != null) {
            throw VascException("Duplicate constructor: $name.$constructor")
        }
        declaredConstructors += constructor
    }

    fun addMethod(method: VascMethod) {
        if (getDeclaredMethod(method.name, method.parameterTypes) != null) {
            throw VascException("Duplicate method: $name.${method.name}")
        }
        declaredMethods += method
    }
}