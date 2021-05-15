package com.luckyhan.studio.richeditor.span.paragraph

import android.text.style.BulletSpan
import com.luckyhan.studio.richeditor.span.RichSpannable

class RichBulletSpan : BulletSpan(), RichSpannable {
    override fun copy(): RichSpannable {
        return RichBulletSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"bullet\">"
    }

}