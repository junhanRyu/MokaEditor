package com.luckyhan.studio.mokaeditor.span.paragraph

import android.text.NoCopySpan
import android.text.style.ParagraphStyle
import android.text.style.StrikethroughSpan
import com.luckyhan.studio.mokaeditor.span.MokaCopyable
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaStrikeThroughParagraphSpan : StrikethroughSpan(), MokaSpan, NoCopySpan, ParagraphStyle {
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