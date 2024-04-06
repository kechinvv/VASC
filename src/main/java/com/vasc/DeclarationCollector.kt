package com.vasc

import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.member.*
import com.vasc.type.VascClass
import com.vasc.type.VascType

class DeclarationCollector : VascParserBaseVisitor<Any>() {

    private val declaredClassNames = mutableSetOf("Integer", "Boolean", "Real", "List", "Array")

    private val typeMap = mutableMapOf<String, VascType>()
    private val typeResolver = VascTypeResolver {
        typeMap[it] ?: throw VascException()
    }

    override fun visitProgram(ctx: ProgramContext): VascTypeResolver {
        ctx.classDeclaration().map {
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
    val typeResolver: VascTypeResolver
) : VascParserBaseVisitor<Unit>() {

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        // TODO: parent
        ctx.classBody().memberDeclaration().forEach {
            it.accept(this)
        }
    }

    override fun visitInitializedVariable(ctx: InitializedVariableContext) {
        val name = ctx.identifier().text
        if (vascClass.getField(name) != null) throw VascException()
        vascClass.fields += VascVariable(name, ctx.className().accept(typeResolver), true)
    }

    override fun visitUninitializedVariable(ctx: UninitializedVariableContext) {
        val name = ctx.identifier().text
        if (vascClass.getField(name) != null) throw VascException()
        vascClass.fields += VascVariable(name, ctx.className().accept(typeResolver), false)
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) {
        val parameters = ctx.parameters().parameter().map {
            VascVariable(it.identifier().text, it.className().accept(typeResolver), true)
        }
        val constructor = VascConstructor(parameters)
        if (vascClass.constructors.contains(constructor)) throw VascException()
        vascClass.constructors += constructor
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
        val name = ctx.identifier().text
        val parameters = ctx.parameters().parameter().map {
            VascVariable(it.identifier().text, it.className().accept(typeResolver), true)
        }
        val returnType = ctx.className().accept(typeResolver)
        val method = VascMethod(name, parameters, returnType)
        if (vascClass.methods.contains(method)) throw VascException()
        vascClass.methods += method
    }
}

private class MutableVascClass(name: String) : VascClass(name) {
    override var parent: VascType? = null
    override val fields = LinkedHashSet<VascVariable>()
    override val methods = LinkedHashSet<VascMethod>()
    override val constructors = LinkedHashSet<VascConstructor>()
}