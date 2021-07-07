package com.luckyhan.studio.mokaeditor.span.character

import android.graphics.Typeface
import android.text.NoCopySpan
import android.text.style.StyleSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaBoldSpan : StyleSpan(Typeface.BOLD), MokaSpan {
    override fun copy(): MokaSpan {
        return MokaBoldSpan()
    }
    override fun getSpanTypeName(): String {
        return "bold"
    }

    override fun writeToJson(json: JSONObject) {
        return
    }
}