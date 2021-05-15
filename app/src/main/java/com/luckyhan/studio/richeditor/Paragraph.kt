package com.luckyhan.studio.richeditor

import android.text.Spannable
import android.text.style.ParagraphStyle
import com.luckyhan.studio.richeditor.span.RichSpannable

class Paragraph(private var start : Int, private var end : Int) {
    private val paragraphSpans : ArrayList<SpanModel> = ArrayList()

    fun storeSpans(spannable : Spannable){
        val spans = spannable.getSpans(start, end, ParagraphStyle::class.java)
        for(span in spans){
            if(span is RichSpannable){
                val spanStart = spannable.getSpanStart(span)
                val spanEnd = spannable.getSpanEnd(span)
                val spanFlag = spannable.getSpanFlags(span)
                val spanModel = SpanModel(span, spanFlag, spanStart, spanEnd)
                paragraphSpans.add(spanModel)
            }
        }
    }
}