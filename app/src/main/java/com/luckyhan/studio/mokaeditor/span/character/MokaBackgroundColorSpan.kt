package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.BackgroundColorSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan

class MokaBackgroundColorSpan(val color : Int) : BackgroundColorSpan(color), MokaSpan, NoCopySpan {
    override fun getSpanTypeName(): String {
        return "backgroundcolor"
    }

    override fun copy(): MokaSpan {
        return MokaBackgroundColorSpan(color)
    }
}