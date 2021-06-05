package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.RelativeSizeSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan

class MokaFontSizeSpan(val proportion : Float) : RelativeSizeSpan(proportion), MokaSpan, NoCopySpan {
    override fun copy(): MokaSpan {
        return MokaFontSizeSpan(proportion)
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"fontsize\" size=\"$proportion\">"
    }
}