package com.luckyhan.studio.richeditor.span.character

import android.text.NoCopySpan
import android.text.style.RelativeSizeSpan
import com.luckyhan.studio.richeditor.span.RichSpan

class RichFontSizeSpan(private val proportion : Float) : RelativeSizeSpan(proportion), RichSpan, NoCopySpan {
    override fun copy(): RichSpan {
        return RichFontSizeSpan(proportion)
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"fontsize\" size=\"$proportion\">"
    }
}