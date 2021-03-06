package com.luckyhan.studio.mokaeditor.span.paragraph

import android.text.style.StrikethroughSpan
import com.luckyhan.studio.mokaeditor.span.MokaCopyable
import com.luckyhan.studio.mokaeditor.span.MokaParagraphStyle
import org.json.JSONObject

class MokaStrikeThroughParagraphSpan : StrikethroughSpan(), MokaParagraphStyle {
    override fun copy(): MokaCopyable {
        return MokaStrikeThroughParagraphSpan()
    }

    override fun getSpanTypeName(): String {
        return "strikethrough_paragraph"
    }

    override fun writeToJson(json: JSONObject) {
        return
    }
}