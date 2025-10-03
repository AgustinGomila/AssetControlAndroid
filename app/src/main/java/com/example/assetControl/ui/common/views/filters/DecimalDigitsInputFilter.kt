package com.example.assetControl.ui.common.views.filters

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) : InputFilter {
    private var mPattern: Pattern =
        Pattern.compile(
            "^[+-]?[0-9]{0," +
                    (when {
                        digitsBeforeZero > 0 -> digitsBeforeZero - 1
                        else -> 0
                    }).toString() +
                    "}(?:[\\.\\,][0-9]{0," +
                    (when {
                        digitsAfterZero > 0 -> digitsAfterZero - 1
                        else -> 0
                    }).toString() +
                    "})?\$"
        )

    /**
     *
    "[0-9]{0," + (when {
    digitsBeforeZero > 0 -> digitsBeforeZero - 1
    else -> 0
    }).toString() + "}+((\\.[0-9]{0," + (when {
    digitsAfterZero > 0 -> digitsAfterZero - 1
    else -> 0
    }).toString() + "})?)||(\\.)?"
     */

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        val matcher = mPattern.matcher(source)
        return when {
            matcher.matches() -> null
            else -> ""
        }
    }
}