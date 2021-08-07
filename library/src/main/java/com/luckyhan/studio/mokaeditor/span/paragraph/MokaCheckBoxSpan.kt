package com.luckyhan.studio.mokaeditor.span.paragraph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.Spannable
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import android.util.Log
import androidx.core.content.ContextCompat
import com.luckyhan.studio.mokaeditor.MokaEditText
import com.luckyhan.studio.mokaeditor.R
import com.luckyhan.studio.mokaeditor.span.MokaClickable
import com.luckyhan.studio.mokaeditor.span.MokaCopyable
import com.luckyhan.studio.mokaeditor.span.MokaParagraphStyle
import com.luckyhan.studio.mokaeditor.util.MokaDisplayUnitUtil
import org.json.JSONObject
import kotlin.math.roundToInt

class MokaCheckBoxSpan(
    private val editText: MokaEditText,
    var checked: Boolean = false,
    private val checkboxWidth: Float = 28f,
    private val checkBoxMarginLeft: Float = 4f,
    private val checkBoxMarginRight: Float = 4f,
    private val checkBoxMarginTop: Float = 2f,
    private val checkBoxMarginBottom: Float = 2f,
) : LeadingMarginSpan, MokaClickable, MokaParagraphStyle, LineHeightSpan {

    companion object {
        val TAG = "MokaCheckBoxSpan"
    }

    private val context: Context = editText.context
    private val spannable: Spannable = editText.text ?: throw UnsupportedOperationException("EditText is not spannable")
    var checkBoxSize = MokaDisplayUnitUtil.getPxFromDp(context, checkboxWidth)
    var unCheckedDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_check_box_unchecked)
    var checkedDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_check_box_checked)
    var marginLeft = MokaDisplayUnitUtil.getPxFromDp(context, checkBoxMarginLeft)
    var marginRight = MokaDisplayUnitUtil.getPxFromDp(context, checkBoxMarginRight)
    var marginTop = MokaDisplayUnitUtil.getPxFromDp(context, checkBoxMarginTop)
    var marginBottom = MokaDisplayUnitUtil.getPxFromDp(context, checkBoxMarginBottom)

    override var clickableLeft: Int = 0
    override var clickableRight: Int = 0
    override var clickableTop: Int = 0
    override var clickableBottom: Int = 0

    override fun onClicked() {
        Log.d(this.javaClass.name, "onClicked")
        val spanStart = spannable.getSpanStart(this)
        val spanEnd = spannable.getSpanEnd(this)
        val other = copy() as MokaCheckBoxSpan
        other.checked = !checked
        spannable.removeSpan(this)
        spannable.setSpan(other, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        if (other.checked) {
            val strikeThroughSpan = MokaStrikeThroughParagraphSpan()
            spannable.setSpan(strikeThroughSpan, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        } else {
            val strikeThroughSpans = spannable.getSpans(spanStart, spanEnd, MokaStrikeThroughParagraphSpan::class.java)
            if (strikeThroughSpans?.isNotEmpty() == true) {
                spannable.removeSpan(strikeThroughSpans[0])
            }
        }
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return (checkBoxSize + marginLeft + marginRight)
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
        val drawable = if (checked) checkedDrawable else unCheckedDrawable
        canvas?.let {
            if ((text as Spanned).getSpanStart(this) == startOffset) {
                clickableLeft = x + marginLeft
                clickableRight = clickableLeft + checkBoxSize
                clickableTop = lineTop + marginTop
                clickableBottom = lineBottom - marginBottom
                drawable?.setBounds(clickableLeft, clickableTop, clickableRight, clickableBottom)
                drawable?.draw(canvas)
            }
        }

    }

    override fun copy(): MokaCopyable {
        return MokaCheckBoxSpan(editText, false, checkboxWidth, checkBoxMarginLeft, checkBoxMarginRight, checkBoxMarginTop, checkBoxMarginBottom)
    }

    override fun getSpanTypeName(): String {
        return "checkbox"
    }

    override fun writeToJson(json: JSONObject) {
        json.put("checked", checked)
    }

    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, lineHeight: Int, fm: Paint.FontMetricsInt?) {
        fm?.let {
            val expectedHeight = checkBoxSize + marginTop + marginBottom
            val originHeight = fm.descent - fm.ascent
            // If original height is not positive, do nothing.
            // If original height is not positive, do nothing.
            Log.d(TAG, "before bottom ${fm.bottom}, top ${fm.top}, ascent ${fm.ascent}, descent ${fm.descent}, ")
            Log.d(TAG, "start : $start, end : $end, spanstart : $spanstartv")
            if (originHeight <= 0) {
                return
            }
            if (originHeight < expectedHeight) {
                val ratio: Float = expectedHeight * 1.0f / originHeight
                fm.descent = (fm.descent * ratio).roundToInt()
                fm.ascent = (fm.ascent * ratio).roundToInt()
                fm.top = fm.ascent
                fm.bottom = fm.descent
                Log.d(
                    TAG,
                    "descent ${fm.descent}, ascent ${fm.ascent}, expect $expectedHeight, origin $originHeight, bottom ${fm.bottom}, top ${fm.top}"
                )
            }
        }
    }
}