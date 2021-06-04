package com.luckyhan.studio.richeditor.span.paragraph

import android.text.NoCopySpan
import android.text.style.ParagraphStyle
import android.text.style.StrikethroughSpan
import com.luckyhan.studio.richeditor.span.RichCopyable
import com.luckyhan.studio.richeditor.span.RichSpan

class RichStrikeThroughSpan : StrikethroughSpan(), RichSpan, NoCopySpan, ParagraphStyle {
    override fun copy(): RichCopyable {
        return RichStrikeThroughSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"strikethrough\">"
    }
}