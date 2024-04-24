package com.vasc

import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.error.VascException
import com.vasc.member.*
import com.vasc.type.*
import com.vasc.util.toUniqueVariables
import com.vasc.util.toVascVariable
import org.antlr.v4.runtime.ParserRuleContext

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

    val classBuilders = mutableMapOf<String, ClassBuilder>()
    val classVisitor: (VascType) -> Unit = {
        val decl = namedDeclarations[it.name]!!
        val builder = classBuilders[it.name]!!
        decl.accept(builder)
    }

    classMap.mapValuesTo(classBuilders) { (_, vascClass) ->
        ClassBuilder(vascClass, typeResolver, errors, classVisitor)
    }

    classMap.values.forEach(classVisitor)

    return typeResolver
}

private class ClassBuilder(
    val vascClass: MutableVascClass,
    val typeResolver: VascTypeResolver,
    val errors: MutableList<VascException>,
    val parentTypeVisitor: (VascType) -> Unit
) : VascParserBaseVisitor<Unit>() {

    val className = vascClass.name

    private var visited = false

    override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
        if (visited) return

        var parentType: VascType? = null
        ctx.parentName?.let { idCtx ->
            tryWithContext(idCtx) { parentType = it.accept(typeResolver) }
        }
        parentType?.let { parent ->
            if (vascClass.isAssignableFrom(parent)) {
                errors.add(VascException("Cyclic inheritance involving '$className'", ctx.parentName))
            } else {
                vascClass.parent = parent
                parentTypeVisitor.invoke(parent)
            }
        }

        ctx.classBody().memberDeclarations.forEach {
            it.accept(this)
        }

        visited = true
    }

    override fun visitFieldDeclaration(ctx: FieldDeclarationContext) = tryWithContext(ctx) {
        val variable = ctx.variableDeclaration().toVascVariable(typeResolver)
        vascClass.addField(variable)
    }

    override fun visitConstructorDeclaration(ctx: ConstructorDeclarationContext) = tryWithContext(ctx) {
        val constructor = VascConstructor(
            parameters = ctx.parameters().toUniqueVariables(typeResolver)
        )
        vascClass.addConstructor(constructor)
    }

    override fun visitMethodDeclaration(ctx: MethodDeclarationContext) = tryWithContext(ctx) {
        val method = VascMethod(
            name = ctx.identifier().text,
            parameters = ctx.parameters().toUniqueVariables(typeResolver),
            returnType = ctx.returnType?.accept(typeResolver) ?: VascVoid
        )
        vascClass.addMethod(method)
    }

    private inline fun tryWithContext(ctx: ParserRuleContext, block: (ParserRuleContext) -> Unit) {
        try {
            block(ctx)
        } catch (e: VascException) {
            errors.add(VascException(e.message, ctx))
        }
    }
}

private class MutableVascClass(name: String) : VascClass(name) {

    override var parent: VascType? = null

    override var declaredFields: MutableSet<VascVariable> = mutableSetOf()
    override var declaredConstructors: MutableSet<VascConstructor> = mutableSetOf()
    override var declaredMethods: MutableSet<VascMethod> = mutableSetOf()

    fun addField(variable: VascVariable) {
        if (!declaredFields.add(variable)) {
            throw VascException("Duplicate field '$name.${variable.name}'")
        }
    }

    fun addConstructor(constructor: VascConstructor) {
        if (!declaredConstructors.add(constructor)) {
            throw VascException("Duplicate constructor '$name.$constructor'")
        }
    }

    fun addMethod(method: VascMethod) {
        if (!declaredMethods.add(method)) {
            throw VascException("Duplicate method '$name.${method.name}'")
        }
        // method is considered overridden if parameter types match exactly
        parent?.methods?.find { method == it }?.let {
            if (!it.returnType.isAssignableFrom(method.returnType)) {
                throw VascException("Return type of '$name.$method' is incompatible with parent")
            }
        }
    }
}