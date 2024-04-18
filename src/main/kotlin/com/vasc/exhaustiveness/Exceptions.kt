package com.vasc.exhaustiveness

import com.vasc.antlr.VascParser.*
import com.vasc.error.VascException

class SuperConstructorCallException(ctx: StatementContext) :
    VascException("Super Constructor must be called first in constructor body", ctx)

class ThisConstructorCallException(ctx: StatementContext) :
    VascException("This Constructor must be called first in constructor body", ctx)

class NonExhaustiveReturnException(ctx: BodyContext) :
    VascException("Nonexhaustive return", ctx)

class UnreachableCodeException(ctx: StatementContext) :
    VascException("Unreachable code", ctx)

class ConstructorNotFound(ctx: ThisExpressionContext) :
    VascException("Constructor not found", ctx)

class CyclicConstructorException(ctx: ThisExpressionContext) :
    VascException("Cyclic constructor usage", ctx)

class DefaultConstructorNotExistException(ctx: BodyContext) :
    VascException("Default constructor not exist. Use super call.", ctx)

class ConstructorsMatchSuperNotExists(ctx: ClassDeclarationContext) :
    VascException("No constructors match super", ctx)
