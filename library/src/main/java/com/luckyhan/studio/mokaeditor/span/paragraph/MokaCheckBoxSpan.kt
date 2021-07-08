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

class MokaCheckBoxSpan(
    private val editText: MokaEditText,
    var checked: Boolean = false,
    private val checkboxWidth : Float = 28f,
    private val checkBoxPaddingLeft: Float = 4f,
    private val checkBoxPaddingRight: Float = 4f,
    private val checkBoxPaddingTop: Float = 2f,
    private val checkBoxPaddingBottom: Float = 2f,
    ) : LeadingMarginSpan, MokaClickable, MokaParagraphStyle, LineHeightSpan {

    companion object{
        val TAG = "MokaCheckBoxSpan"
    }

    private val context: Context = editText.context
    private val spannable: Spannable = editText.text ?: throw UnsupportedOperationException("EditText is not spannable")
    var checkBoxSize = MokaDisplayUnitUtil.getPxFromDp(context, checkboxWidth)
    var unCheckedDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_check_box_unchecked)
    var checkedDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_check_box_checked)
    var paddingLeft = MokaDisplayUnitUtil.getPxFromDp(context, checkBoxPaddingLeft)
    var paddingRight = MokaDisplayUnitUtil.getPxFromDp(context, checkBoxPaddingRight)
    var paddingTop = MokaDisplayUnitUtil.getPxFromDp(context, checkBoxPaddingTop)
    var paddingBottom = MokaDisplayUnitUtil.getPxFromDp(context, checkBoxPaddingBottom)

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
        return (checkBoxSize + paddingLeft + paddingRight)
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
                clickableLeft = x + paddingLeft
                clickableRight = clickableLeft + checkBoxSize
                clickableTop = lineTop + ((lineBottom - lineTop - checkBoxSize) / 2)
                clickableBottom = clickableTop + checkBoxSize
                drawable?.setBounds(clickableLeft, clickableTop, clickableRight, clickableBottom)
                drawable?.draw(canvas)
            }
        }

    }

    override fun copy(): MokaCopyable {
        return MokaCheckBoxSpan(editText)
    }

    override fun getSpanTypeName(): String {
        return "checkbox"
    }

    override fun writeToJson(json: JSONObject) {
        json.put("checked", checked)
    }

    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, lineHeight: Int, fm: Paint.FontMetricsInt?) {
        fm?.let{
            val checkBoxHeight = checkBoxSize
            val originHeight = fm.descent - fm.ascent
            // If original height is not positive, do nothing.
            // If original height is not positive, do nothing.
            if (originHeight <= 0) {
                return
            }
            Log.d(TAG, "origin : $originHeight , checkbox height : $checkBoxHeight")
            if(originHeight < checkBoxHeight){
                Log.d(TAG, "height is adjusted")
                val ratio: Float = checkBoxHeight * 1.0f / originHeight
                fm.descent = Math.round(fm.descent * ratio)
                fm.ascent = fm.descent - checkBoxHeight.toInt()
            }
        }
    }
}