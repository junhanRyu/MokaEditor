package com.luckyhan.studio.mokaeditor.span.paragraph

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.luckyhan.studio.mokaeditor.span.MokaParagraphStyle
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaQuoteSpan(
    private val mColor : Int = Color.BLACK,
    private val mStripeWidth : Int = 10,
    private val mGapWidth : Int = 80)
    : MokaParagraphStyle, LeadingMarginSpan {
    override fun copy(): MokaSpan {
        return MokaQuoteSpan()
    }

    override fun getSpanTypeName(): String {
        return "quote"
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
        canvas?.let {
            paint?.let {
                val style = paint.style
                val color = paint.color
                paint.style = Paint.Style.FILL
                paint.color = mColor
                val xPosition = (x+mGapWidth-mStripeWidth)/2f
                canvas.drawRect(xPosition, lineTop.toFloat(), (xPosition + dir * mStripeWidth), lineBottom.toFloat(), paint)
                paint.style = style
                paint.color = color
            }
        }
    }
}