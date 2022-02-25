package com.github.globocom.viewport.commons

import androidx.annotation.FloatRange

sealed class Threshold(val proportion: Float) {
    object Visible : Threshold(1f)
    object Half : Threshold(0.5f)
    object AlmostHidden : Threshold(0.25f)
    object AlmostVisible : Threshold(0.75f)
    class Custom(@FloatRange(from = 0.0, to = 1.0) proportion: Float) : Threshold(proportion)

    override fun toString(): String {
        return this::class.simpleName ?: super.toString()
    }

    companion object {
        fun fromProportionValue(proportion: Float) = listOf(
            Visible, Half, AlmostHidden, AlmostVisible
        ).firstOrNull { it.proportion == proportion } ?: Custom(proportion)

        fun values() = Threshold::class.sealedSubclasses.filter { c -> c != Custom::class }
            .map { it.objectInstance }
    }
}
