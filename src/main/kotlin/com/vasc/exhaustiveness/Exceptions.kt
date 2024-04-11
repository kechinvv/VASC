package com.vasc.exhaustiveness

import com.vasc.error.VascException

class SuperConstructorCallException(message: String) : VascException(message)
class ThisConstructorCallException(message: String) : VascException(message)
class NonExhaustiveReturnException(message: String) : VascException(message)
class UnreachableCodeException(message: String) : VascException(message)
class ConstructorNotFound(message: String) : VascException(message)
class CyclicConstructorException(message: String) : VascException(message)
class DefaultConstructorNotExistException(message: String) : VascException(message)
class ConstructorsMatchSuperNotExists(message: String) : VascException(message)
