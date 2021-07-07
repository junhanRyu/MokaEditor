package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.BackgroundColorSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaBackgroundColorSpan(val color : Int) : BackgroundColorSpan(color), MokaSpan {
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