package com.luckyhan.studio.richeditor.span.paragraph

import android.text.style.BulletSpan
import com.luckyhan.studio.richeditor.span.RichSpan

class RichBulletSpan : BulletSpan(), RichSpan {
    override fun copy(): RichSpan {
        return RichBulletSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"bullet\">"
    }

}