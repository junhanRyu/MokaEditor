package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.UnderlineSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan

class MokaUnderlineSpan : UnderlineSpan(), MokaSpan, NoCopySpan {
    override fun copy(): MokaSpan {
        return MokaUnderlineSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"underline\">"
    }
}