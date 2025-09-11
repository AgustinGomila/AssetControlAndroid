package com.example.assetControl.ui.common.views.filters

import android.text.InputFilter
import android.text.Spanned

@Suppress("unused")
class InputFilterMinMax : InputFilter {
    private var min: Int = 0
    private var max: Int = 0

    constructor(min: Int, max: Int) {
        this.min = min
        this.max = max
    }

    constructor(min: String, max: String) {
        this.min = java.lang.Double.parseDouble(min).toInt()
        this.max = java.lang.Double.parseDouble(max).toInt()
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        try {
            // Remove the string out of destination that is to be replaced
            var newVal = dest.toString().substring(0, dstart) + dest.toString()
                .substring(dend, dest.toString().length)
            // Add the new string in
            newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(
                dstart,
                newVal.length
            )
            val input = Integer.parseInt(newVal)
            if (isInRange(min, max, input))
                return null
        } catch (nfe: NumberFormatException) {
        }

        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}