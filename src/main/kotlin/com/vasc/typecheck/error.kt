package com.vasc.typecheck

import com.vasc.antlr.VascParser.*
import com.vasc.error.VascException
import com.vasc.type.VascType
import org.antlr.v4.runtime.ParserRuleContext

open class TypeCheckException(description: String, ctx: ParserRuleContext) : VascException("TYPE ERROR:\n$description", ctx)

class UnexpectedTypeException(expected: VascType, actual: VascType, ctx: ParserRuleContext) :
    TypeCheckException("""
        |expected type:
        |  $expected
        |but got:
        |  $actual
    """.trimMargin(), ctx)

class UnnecessaryReturnException(ctx: ReturnStatementContext) :
    TypeCheckException("unnecessary return inside method without return type", ctx)

class ParentNotFoundException(className: String, ctx: SuperExpressionContext) :
    TypeCheckException("class '$className' does not have a parent", ctx)

class ConstructorNotFoundException(className: String, args: List<VascType>, ctx: ParserRuleContext) :
    TypeCheckException("constructor not found '$className(${args.joinToString(",")})'", ctx)

class MethodNotFoundException(className: String, methodName: String, args: List<VascType>, ctx: ParserRuleContext) :
    TypeCheckException("method not found '$className.$methodName(${args.joinToString(",")})'", ctx)

class MethodReturnsNoValueException(className: String, methodName: String, ctx: ParserRuleContext) :
    TypeCheckException("method '$className.$methodName' does not return a value", ctx)

class UnknownVariableException(name: String, ctx: IdentifierContext) : // TODO: move from type check?
    TypeCheckException("unknown variable '$name'", ctx)

class UnexpectedNullException(ctx: NullLiteralContext) :
    TypeCheckException("unexpected null", ctx)

class ExpressionHasNoValueException(ctx: ExpressionContext) :
    TypeCheckException("expression has no value", ctx)

class FieldNotFoundException(className: String, fieldName: String, ctx: ParserRuleContext) :
    TypeCheckException("field not found '$className.$fieldName'", ctx)
