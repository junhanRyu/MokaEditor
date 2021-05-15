package com.luckyhan.studio.richeditor.span.character

import android.graphics.Typeface
import android.text.style.StyleSpan
import com.luckyhan.studio.richeditor.span.RichSpannable

class RichBoldSpan : StyleSpan(Typeface.BOLD), RichSpannable {
    override fun copy(): RichSpannable {
        return RichBoldSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"bold\">"
    }


}