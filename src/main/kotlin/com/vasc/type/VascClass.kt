package com.vasc.type

import com.vasc.error.VascException
import com.vasc.member.*

open class VascClass(
    final override val name: String,
    override val parent: VascType? = null
) : VascType {

    override val declaredFields: Collection<VascVariable> = emptySet()
    override val declaredConstructors: Collection<VascConstructor> = emptySet()
    override val declaredMethods: Collection<VascMethod> = emptySet()

    final override val fields: Collection<VascVariable>
        get() = declaredFields.toSet() + (parent?.fields ?: emptySet())

    final override val methods: Collection<VascMethod>
        get() = declaredMethods.toSet() + (parent?.methods ?: emptySet())

    final override fun getDeclaredField(name: String): VascVariable? {
        return declaredFields.find { it.name == name }
    }

    final override fun getDeclaredConstructor(parameterTypes: List<VascType>): VascConstructor? {
        return getMostSpecific(parameterTypes, declaredConstructors)
    }

    final override fun getDeclaredMethod(name: String, parameterTypes: List<VascType>): VascMethod? {
        return getMostSpecific(parameterTypes, declaredMethods.filter { it.name == name })
    }

    final override fun getField(name: String): VascVariable? {
        return getDeclaredField(name) ?: parent?.getField(name)
    }

    final override fun getMethod(name: String, parameterTypes: List<VascType>): VascMethod? {
        return getMostSpecific(parameterTypes, methods.filter { it.name == name })
    }

    final override fun isAssignableFrom(subtype: VascType): Boolean {
        return subtype.inheritanceDistanceTo(this) >= 0
    }

    private fun <P : VascParametrized> getMostSpecific(params: List<VascType>, all: Collection<P>): P? {
        val applicable = all.filter { it.isApplicableTo(params) }
        if (applicable.isEmpty()) return null
        if (applicable.size == 1) return applicable.first()

        class RankingResult(val parametrized: P, val score: Int)

        val candidates = applicable.map {
            val score = params.indices.fold(0) { score, i ->
                score + params[i].inheritanceDistanceTo(it.parameterTypes[i])
            }
            RankingResult(it, score)
        }
        val bestScore = candidates.minOf { it.score }
        val finalists = candidates.mapNotNull { res ->
            res.parametrized.takeIf { res.score == bestScore }
        }

        if (finalists.size > 1) {
            throw VascException(
                "Ambiguous call. There are ${finalists.size} matches for $name:\n\t${finalists.joinToString("\n\t")}"
            )
        }

        return finalists.first()
    }

    /**
     * Called on child
     */
    private fun VascType.inheritanceDistanceTo(other: VascType): Int {
        if (this is VascNull) return 0

        var distance = 0
        var current: VascType? = this
        while (current != null) {
            if (current == other) return distance
            current = current.parent
            distance++
        }
        return -1
    }

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VascType) return false

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}