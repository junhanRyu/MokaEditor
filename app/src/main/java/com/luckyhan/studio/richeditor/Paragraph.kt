package com.luckyhan.studio.richeditor

import android.text.Spannable
import android.text.style.ParagraphStyle

class Paragraph(var start : Int, var end : Int) {
    /*private val paragraphSpans : ArrayList<SpanModel> = ArrayList()

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
        for(spanModel in paragraphSpans){
            spannable.removeSpan(spanModel.span)
        }
    }

    fun restoreSpans(spannable : Spannable, newStart : Int, newEnd : Int){
        if(newEnd - newStart <= 0){
            return
        }
        for(spanModel in paragraphSpans){
            spannable.setSpan(spanModel.span, newStart, newEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun copySpans(spannable : Spannable, newStart : Int, newEnd : Int){
        if(newEnd - newStart <= 0){
            return
        }
        for(spanModel in paragraphSpans){
            val otherSpan = spanModel.span.copy()
            spannable.setSpan(otherSpan, newStart, newEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun isSpans() : Boolean{
        return paragraphSpans.isNotEmpty()
    }*/
}