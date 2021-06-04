package com.luckyhan.studio.richeditor.span.character

import android.graphics.Typeface
import android.text.NoCopySpan
import android.text.style.StyleSpan
import com.luckyhan.studio.richeditor.span.RichSpan

class RichBoldSpan : StyleSpan(Typeface.BOLD), RichSpan, NoCopySpan {
    override fun copy(): RichSpan {
        return RichBoldSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"bold\">"
    }
}