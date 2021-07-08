package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.ForegroundColorSpan
import com.luckyhan.studio.mokaeditor.span.MokaCharacterStyle
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaForegroundColorSpan(val color : Int) : ForegroundColorSpan(color), MokaCharacterStyle {
    override fun copy(): MokaSpan {
        return MokaForegroundColorSpan(color)
    }

    override fun getSpanTypeName(): String {
        return "foregroundcolor"
    }

    override fun writeToJson(json: JSONObject) {
        json.put("color", color)
    }
}