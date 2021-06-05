package com.luckyhan.studio.mokaeditor.span.character

import android.graphics.Typeface
import android.text.NoCopySpan
import android.text.style.StyleSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan

class MokaBoldSpan : StyleSpan(Typeface.BOLD), MokaSpan, NoCopySpan {
    override fun copy(): MokaSpan {
        return MokaBoldSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"bold\">"
    }
}