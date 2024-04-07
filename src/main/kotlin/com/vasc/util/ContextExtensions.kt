package com.vasc.util

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.ParameterContext
import com.vasc.antlr.VascParser.ParametersContext
import com.vasc.antlr.VascParser.VariableDeclarationContext
import com.vasc.error.VascException
import com.vasc.member.VascVariable

fun VariableDeclarationContext.toVascVariable(typeResolver: VascTypeResolver): VascVariable {
    return VascVariable(
        name = identifier().text,
        type = className().accept(typeResolver)
    )
}

// Don't try to generalize `VariableDeclarationContext` with `ParameterContext` in grammar until we are 100% sure,
// that the concept of treating parameters like variables doesn't have any pitfalls
fun ParameterContext.toVascVariable(typeResolver: VascTypeResolver): VascVariable {
    return VascVariable(
        name = identifier().text,
        type = className().accept(typeResolver)
    )
}

fun ParametersContext.toUniqueVariables(typeResolver: VascTypeResolver): List<VascVariable> {
    val variableMap = mutableMapOf<String, VascVariable>()
    for (param in parameter()) {
        val variable = param.toVascVariable(typeResolver)
        val prevValue = variableMap.put(variable.name, variable)
        if (prevValue != null) throw VascException()
    }
    return variableMap.values.toList()
}