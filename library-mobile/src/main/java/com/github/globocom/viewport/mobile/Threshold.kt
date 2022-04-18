package com.github.globocom.viewport.mobile

enum class Threshold(val proportion: Float) {
    VISIBLE(1f),
    HALF(0.5f),
    ALMOST_HIDDEN(0.25f),
    ALMOST_VISIBLE(0.75f);

    companion object {
        fun fromProportionValue(proportion: Float) =
            values().firstOrNull { it.proportion == proportion }
    }
}
