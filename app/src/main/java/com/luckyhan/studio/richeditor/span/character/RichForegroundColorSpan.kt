package com.luckyhan.studio.richeditor.span.character

import android.text.NoCopySpan
import android.text.style.ForegroundColorSpan
import com.luckyhan.studio.richeditor.span.RichSpan

class RichForegroundColorSpan(private val color : Int) : ForegroundColorSpan(color), RichSpan, NoCopySpan {
    override fun copy(): RichSpan {
        return RichForegroundColorSpan(color)
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"foregroundcolor\" color=\"$color\">"
    }
}