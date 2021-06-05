package com.luckyhan.studio.richeditor.span.character

import android.text.NoCopySpan
import android.text.style.StrikethroughSpan
import com.luckyhan.studio.richeditor.span.RichSpan

class RichStrikethroughSpan : StrikethroughSpan(), RichSpan, NoCopySpan {
    override fun copy(): RichSpan {
        return RichStrikethroughSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"strikethrough\">"
    }
}