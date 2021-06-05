package com.luckyhan.studio.richeditor.span.paragraph

import android.text.NoCopySpan
import android.text.style.ParagraphStyle
import android.text.style.StrikethroughSpan
import com.luckyhan.studio.richeditor.span.RichCopyable
import com.luckyhan.studio.richeditor.span.RichSpan

class RichStrikeThroughParagraphSpan : StrikethroughSpan(), RichSpan, NoCopySpan, ParagraphStyle {
    override fun copy(): RichCopyable {
        return RichStrikeThroughParagraphSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"strikethrough_paragraph\">"
    }
}