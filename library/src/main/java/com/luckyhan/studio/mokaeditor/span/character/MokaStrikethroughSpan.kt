package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.StrikethroughSpan
import com.luckyhan.studio.mokaeditor.span.MokaCharacterStyle
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaStrikethroughSpan : StrikethroughSpan(), MokaCharacterStyle {
    override fun copy(): MokaSpan {
        return MokaStrikethroughSpan()
    }

    override fun getSpanTypeName(): String {
        return "strikethrough"
    }

    override fun writeToJson(json: JSONObject) {
        return
    }
}