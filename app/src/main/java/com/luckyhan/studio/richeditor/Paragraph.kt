package com.luckyhan.studio.richeditor

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ParagraphStyle
import com.luckyhan.studio.richeditor.span.RichSpannable

class Paragraph(private var start : Int, private var end : Int) {
    private val paragraphSpans : ArrayList<SpanModel> = ArrayList()

    companion object{
        const val zeroWidthChar = "\u200B"
    }

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

    fun removeSpans(spannable : Spannable){
        for(span in paragraphSpans){
            spannable.removeSpan(span)
        }
        if(paragraphSpans.isNotEmpty()){
            if(spannable is SpannableStringBuilder){
                if(spannable.substring(start, start+1) == zeroWidthChar){
                    spannable.delete(start, start+1) // remove zero width character
                    start--
                    end--
                }
            }
        }
    }
}