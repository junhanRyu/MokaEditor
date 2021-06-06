package com.luckyhan.studio.mokaeditor

import android.text.Spannable
import com.luckyhan.studio.mokaeditor.span.MokaSpan

class MokaSpanCollector {
    private val spans : ArrayList<MokaSpanModel> = ArrayList()
    var text : String = ""
        private set

    data class MokaSpanModel(val span : MokaSpan, val start : Int, val end : Int, val flag : Int)

    fun collect(spannable : Spannable){
        text = spannable.toString()
        val spans = spannable.getSpans(0, spannable.length, MokaSpan::class.java)
        spans.forEach {
            val span = MokaSpanModel(it, spannable.getSpanStart(it), spannable.getSpanEnd(it), spannable.getSpanFlags(it))
            this.spans.add(span)
            spannable.removeSpan(it)
        }
    }

    fun <T>getSpans(start : Int, end : Int, classType : Class<T>) : List<MokaSpanModel>{
        return spans
            .filter { (classType.isAssignableFrom(it.span.javaClass) && it.start >= start && it.end <= end) }
    }
}