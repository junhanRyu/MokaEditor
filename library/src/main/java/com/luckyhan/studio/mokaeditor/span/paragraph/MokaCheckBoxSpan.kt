package com.luckyhan.studio.mokaeditor.span.paragraph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.NoCopySpan
import android.text.Spannable
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import com.luckyhan.studio.mokaeditor.MokaEditText
import com.luckyhan.studio.mokaeditor.util.MokaDisplayUnitUtil
import com.luckyhan.studio.mokaeditor.R
import com.luckyhan.studio.mokaeditor.span.MokaClickable
import com.luckyhan.studio.mokaeditor.span.MokaCopyable
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject
import java.lang.UnsupportedOperationException

class MokaCheckBoxSpan(
    private val editText: MokaEditText,
    var checked: Boolean = false,
    private val margin: Int = 100
) : LeadingMarginSpan.Standard(margin), MokaClickable, NoCopySpan, MokaSpan {

    private val context: Context = editText.context
    private val spannable: Spannable = editText.text ?: throw UnsupportedOperationException("EditText is not spannable")
    var checkBoxSize = MokaDisplayUnitUtil.getPxFromDp(context, 24f)
    var unCheckedDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_check_box_unchecked)
    var checkedDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_check_box_checked)
    var paddingLeft = MokaDisplayUnitUtil.getPxFromDp(context, 2f)
    var paddingRight = MokaDisplayUnitUtil.getPxFromDp(context, 2f)

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
        if(other.checked){
            val strikeThroughSpan = MokaStrikeThroughParagraphSpan()
            spannable.setSpan(strikeThroughSpan, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }else{
            val strikeThroughSpans = spannable.getSpans(spanStart, spanEnd, MokaStrikeThroughParagraphSpan::class.java)
            if(strikeThroughSpans?.isNotEmpty() == true){
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
                Log.d(this.javaClass.name, "$clickableRight, line top : $lineTop, line bottom : $lineBottom")
                Log.d(this.javaClass.name, "left : $clickableLeft, right : $clickableRight, top : $clickableTop, bottom : $clickableBottom")
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
}