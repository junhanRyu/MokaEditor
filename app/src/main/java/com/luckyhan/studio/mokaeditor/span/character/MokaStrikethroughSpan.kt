package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.StrikethroughSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan

class MokaStrikethroughSpan : StrikethroughSpan(), MokaSpan, NoCopySpan {
    override fun copy(): MokaSpan {
        return MokaStrikethroughSpan()
    }

    override fun getSpanTypeName(): String {
        return "strikethrough"
    }
}