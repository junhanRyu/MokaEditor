package com.luckyhan.studio.richeditor.span.character

import android.text.NoCopySpan
import android.text.style.UnderlineSpan
import com.luckyhan.studio.richeditor.span.RichSpan

class RichUnderlineSpan : UnderlineSpan(), RichSpan, NoCopySpan {
    override fun copy(): RichSpan {
        return RichUnderlineSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"underline\">"
    }
}