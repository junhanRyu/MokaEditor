package com.luckyhan.studio.mokaeditor

import android.text.Spannable
import com.luckyhan.studio.mokaeditor.span.MokaSpan

class MokaSpanCollector {
    private val spans : ArrayList<RichSpanModel> = ArrayList()
    private var text : String = ""

    data class RichSpanModel(val span : MokaSpan, val start : Int, val end : Int, val flag : Int)

    fun collect(spannable : Spannable){
        text = spannable.toString()
        val spans = spannable.getSpans(0, spannable.length, MokaSpan::class.java)
        spans.forEach {
            val span = RichSpanModel(it, spannable.getSpanStart(it), spannable.getSpanEnd(it), spannable.getSpanFlags(it))
            this.spans.add(span)
            spannable.removeSpan(it)
        }
    }

    fun getText() : String{
        return text
    }

    fun <T>getSpans(start : Int, end : Int, classType : Class<T>) : List<RichSpanModel>{
        return spans
            .filter { (classType.isAssignableFrom(it.span.javaClass) && it.start >= start && it.end <= end) }
    }
}