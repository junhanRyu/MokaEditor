package com.luckyhan.studio.mokaeditor.span.character

import android.text.NoCopySpan
import android.text.style.UnderlineSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaUnderlineSpan : UnderlineSpan(), MokaSpan, NoCopySpan {
    override fun copy(): MokaSpan {
        return MokaUnderlineSpan()
    }

    override fun getSpanTypeName(): String {
        return "underline"
    }

    override fun writeToJson(json: JSONObject) {
        return
    }
}