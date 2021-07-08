package com.luckyhan.studio.mokaeditor.span.paragraph

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.util.Log
import com.luckyhan.studio.mokaeditor.span.MokaParagraphStyle
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaBulletSpan(
    private val mGapWidth: Int = 100,
    private val mColor: Int = Color.BLACK,
    private val mRadius: Float = 8f
) : LeadingMarginSpan, MokaParagraphStyle {
    companion object {
        val TAG = "MokaBulletSpan"
    }

    override fun copy(): MokaSpan {
        return MokaBulletSpan()
    }

    override fun getSpanTypeName(): String {
        return "bullet"
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
        if ((text as Spanned).getSpanStart(this) == startOffset) {
            canvas?.let {
                paint?.let {
                    val style = paint.style
                    val oldColor = paint.color
                    paint.color = mColor
                    paint.style = Paint.Style.FILL
                    val yPosition = (lineTop + lineBottom) / 2f
                    val xPosition: Float = x + (mGapWidth / 2f)
                    canvas.drawCircle(xPosition, yPosition, mRadius, paint)
                    paint.color = oldColor
                    paint.style = style
                }
            }
        }

    }

}