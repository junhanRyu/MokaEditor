package com.luckyhan.studio.mokaeditor.span.paragraph

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.luckyhan.studio.mokaeditor.span.MokaParagraphStyle
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaIndentationSpan(
    private val mGapWidth : Int = 80)
    : MokaParagraphStyle, LeadingMarginSpan {
    override fun copy(): MokaSpan {
        return MokaIndentationSpan()
    }

    override fun getSpanTypeName(): String {
        return "indentation"
    }

    override fun writeToJson(json: JSONObject) {
        return
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return mGapWidth
    }

    override fun drawLeadingMargin(
        canvas: Canvas?,
        paint: Paint?,
        x: Int,
        dir: Int,
        lineTop: Int,
        baseline: Int,
        lineBottom: Int,
        text: CharSequence?,
        startOffset: Int,
        endOffset: Int,
        first: Boolean,
        layout: Layout?
    ) {
       //do nothing
    }
}