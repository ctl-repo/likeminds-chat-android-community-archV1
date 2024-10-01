package com.likeminds.chatmm.finxrecommendation.presentation.helper

import android.text.InputFilter
import android.text.Spanned

class DecimalInputFilter(private val min: Float, private val max: Float) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val destStr = dest.toString()
        val newValue = (destStr + source).toFloatOrNull() ?: return null

        return if (newValue in min..max) {
            null // Accept the input
        } else {
            "" // Reject the input
        }
    }
}

class DecimalDigitsInputFilter(
    private val maxDigitsBeforeDecimal: Int,
    private val maxDigitsAfterDecimal: Int
) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (source == null) return null

        val currentText = dest.toString()

        // Allow backspace
        if (source.isEmpty()) {
            return null
        }

        // Form the complete text by replacing the existing text with the new input
        val newText = currentText.substring(0, dstart) + source + currentText.substring(dend)

        // Check if the new text is a valid decimal number
        if (newText.contains(".")) {
            val parts = newText.split(".")

            // Ensure no more than the allowed digits before the decimal
            if (parts[0].length > maxDigitsBeforeDecimal) {
                return ""
            }

            // Ensure no more than the allowed digits after the decimal
            if (parts.size > 1 && parts[1].length > maxDigitsAfterDecimal) {
                return ""
            }
        } else {
            // Ensure no more than the allowed digits before the decimal if there is no decimal point yet
            if (newText.length > maxDigitsBeforeDecimal) {
                return ""
            }
        }

        // Prevent leading zeroes (like 01.23)
        if (newText.length > 1 && newText.startsWith("0") && !newText.startsWith("0.")) {
            return ""
        }

        // Allow only digits and a single decimal point
        return if (newText.matches(Regex("^\\d*\\.?\\d*$"))) null else ""
    }
}

