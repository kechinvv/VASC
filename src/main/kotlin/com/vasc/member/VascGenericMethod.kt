package com.vasc.member

import com.vasc.type.VascType

class VascGenericMethod(
    name: String,
    val maybeGenericParameters: List<MaybeGeneric<VascVariable>>,
    val maybeGenericReturnType: MaybeGeneric<VascType>
) : VascMethod(name, maybeGenericParameters.map { it.v }, maybeGenericReturnType.v)

sealed class MaybeGeneric<T>(open val v: T)

data class Generic<T>(override val v: T) : MaybeGeneric<T>(v)
data class Concrete<T>(override val v: T) : MaybeGeneric<T>(v)
