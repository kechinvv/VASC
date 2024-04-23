package com.vasc

import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.member.*
import com.vasc.type.*
import com.vasc.util.toUniqueVariables
import com.vasc.util.toVascVariable

fun createTypeResolver(ctx: ProgramContext, errors: MutableList<VascException>): VascTypeResolver {
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
        val variable = ctx.variableDeclaration().toVascVariable(typeResolver)
        if (!vascClass.addField(variable)) {
            errors.add(VascException("Duplicate field: ${vascClass.name}.${variable.name}", ctx))
        }
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) {
        val constructor = VascConstructor(
            parameters = ctx.parameters().toUniqueVariables(typeResolver)
        )
        if (!vascClass.addConstructor(constructor)) {
            errors.add(VascException("Duplicate constructor: ${vascClass.name}.$constructor", ctx))
        }
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        val method = VascMethod(
            name = ctx.identifier().text,
            parameters = ctx.parameters().toUniqueVariables(typeResolver),
            returnType = ctx.returnType?.accept(typeResolver) ?: VascVoid
        )
        if (!vascClass.addMethod(method)) {
            errors.add(VascException("Duplicate method: ${vascClass.name}.${method.name}", ctx))
        }
    }
}

private class MutableVascClass(name: String) : VascClass(name) {

    override var parent: VascType? = null

    override var declaredFields: MutableList<VascVariable> = mutableListOf()
    override var declaredConstructors: MutableList<VascConstructor> = mutableListOf()
    override var declaredMethods: MutableList<VascMethod> = mutableListOf()

    fun addField(variable: VascVariable): Boolean {
        return getDeclaredField(variable.name) == null && declaredFields.add(variable)
    }

    fun addConstructor(constructor: VascConstructor): Boolean {
        val types = getDeclaredConstructor(constructor.parameterTypes)?.parameterTypes
        return types != constructor.parameterTypes && declaredConstructors.add(constructor)
    }

    fun addMethod(method: VascMethod): Boolean {
        val types = getDeclaredMethod(method.name, method.parameterTypes)?.parameterTypes
        return types != method.parameterTypes && declaredMethods.add(method)
    }
}