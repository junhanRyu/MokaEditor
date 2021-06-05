package com.luckyhan.studio.richeditor.span.character

import android.text.NoCopySpan
import android.text.style.BackgroundColorSpan
import com.luckyhan.studio.richeditor.span.RichSpan

class RichBackgroundColorSpan(private val color : Int) : BackgroundColorSpan(color), RichSpan, NoCopySpan {
    override fun copy(): RichSpan {
        return RichBackgroundColorSpan(color)
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"backgroundcolor\" color=\"$color\">"
    }
}