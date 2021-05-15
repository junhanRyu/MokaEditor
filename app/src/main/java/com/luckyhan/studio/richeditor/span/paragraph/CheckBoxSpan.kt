package com.luckyhan.studio.richeditor.span.paragraph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.util.Log
import androidx.core.content.ContextCompat
import com.luckyhan.studio.richeditor.DisplayUnitUtil
import com.luckyhan.studio.richeditor.R

class CheckBoxSpan : LeadingMarginSpan.Standard{
    private var context : Context? = null
    var checked : Boolean = false
    private var checkBoxTop = 0;
    private var checkBoxBottom = 0;
    private var checkBoxLeft = 0;
    private var checkBoxRight = 0;
    private var checkBoxSize = 0;
    private var unCheckedDrawable : Drawable?
    private var checkedDrawable : Drawable?
    private var paddingLeft = 0;
    private var paddingRight = 0;

    constructor(context : Context, checked : Boolean = false) : super(100){
        this.context = context
        this.checked = checked
        checkBoxSize = DisplayUnitUtil.getPxFromDp(context, 24f)
        paddingLeft = DisplayUnitUtil.getPxFromDp(context, 4f)
        paddingRight = DisplayUnitUtil.getPxFromDp(context, 4f)
        unCheckedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_check_box_unchecked)
        checkedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_check_box_checked)
    }

    public fun isChecked(x : Int, y : Int):Boolean{
        return ((y in checkBoxTop..checkBoxBottom) && (x in checkBoxLeft..checkBoxRight))
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return (checkBoxSize+paddingLeft+paddingRight)
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
        val drawable = if(checked) checkedDrawable else unCheckedDrawable
        canvas?.let{
            if((text as Spanned).getSpanStart(this) == startOffset){
                checkBoxLeft = x+paddingLeft
                checkBoxRight = checkBoxLeft+checkBoxSize
                checkBoxTop = lineTop+(((lineBottom-lineTop-checkBoxSize)/2))
                checkBoxBottom = checkBoxTop+checkBoxSize
                Log.d("span", "top: ${checkBoxTop}, bottom: ${checkBoxBottom}, left : ${checkBoxLeft}, right: ${checkBoxRight}")
                Log.d("span", "start : ${startOffset}, end : ${endOffset}, first: ${first}")
                Log.d("span", "lineTop : ${lineTop}, lineBottom : ${lineBottom}")
                drawable?.setBounds(checkBoxLeft, checkBoxTop, checkBoxRight, checkBoxBottom)
                drawable?.draw(canvas)
            }
        }

    }
}