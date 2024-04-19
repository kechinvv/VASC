package com.vasc.exhaustiveness

import com.vasc.antlr.VascParser.*
import com.vasc.error.VascException

class IllegalSuperConstructorCallException(ctx: StatementContext) :
    VascException("Super Constructor must be called first in constructor body", ctx)

class IllegalThisConstructorCallException(ctx: StatementContext) :
    VascException("This Constructor must be called first in constructor body", ctx)

class NonExhaustiveReturnException(ctx: BodyContext) :
    VascException("Non exhaustive return", ctx)

class UnreachableCodeException(ctx: StatementContext) :
    VascException("Unreachable code", ctx)

class ConstructorNotFound(ctx: ThisExpressionContext) :
    VascException("Constructor not found", ctx)

class CyclicConstructorException(ctx: ThisExpressionContext) :
    VascException("Cyclic constructor usage", ctx)

class DefaultConstructorNotExistsException(ctx: BodyContext) :
    VascException("Default constructor not exists. Use super call.", ctx)

class ConstructorsMatchSuperNotExists(ctx: ClassDeclarationContext) :
    VascException("No constructors match super", ctx)
