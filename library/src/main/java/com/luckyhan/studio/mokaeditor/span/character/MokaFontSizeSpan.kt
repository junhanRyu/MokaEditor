package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.RelativeSizeSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaFontSizeSpan(val proportion : Float) : RelativeSizeSpan(proportion), MokaSpan {
    override fun copy(): MokaSpan {
        return MokaFontSizeSpan(proportion)
    }

    override fun getSpanTypeName(): String {
        return "fontsize"
    }

    override fun writeToJson(json: JSONObject) {
        json.put("proportion", proportion.toDouble())
    }
}