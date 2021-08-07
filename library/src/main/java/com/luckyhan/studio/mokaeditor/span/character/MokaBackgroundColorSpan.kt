package com.luckyhan.studio.mokaeditor.span.character

import android.text.style.BackgroundColorSpan
import com.luckyhan.studio.mokaeditor.span.MokaCharacterStyle
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaBackgroundColorSpan(val color : Int) : BackgroundColorSpan(color), MokaCharacterStyle {
    override fun getSpanTypeName(): String {
        return "backgroundcolor"
    }

    override fun writeToJson(json: JSONObject) {
        json.put("color", color)
    }

    override fun copy(): MokaSpan {
        return MokaBackgroundColorSpan(color)
    }
}