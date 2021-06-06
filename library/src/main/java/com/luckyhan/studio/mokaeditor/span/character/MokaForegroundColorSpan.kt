package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.ForegroundColorSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan

class MokaForegroundColorSpan(val color : Int) : ForegroundColorSpan(color), MokaSpan, NoCopySpan {
    override fun copy(): MokaSpan {
        return MokaForegroundColorSpan(color)
    }

    override fun getSpanTypeName(): String {
        return "foregroundcolor"
    }
}