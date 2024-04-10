package com.vasc.error

class SuperConstructorCallException(override val message: String?) : VascException()
class ThisConstructorCallException(override val message: String?) : VascException()