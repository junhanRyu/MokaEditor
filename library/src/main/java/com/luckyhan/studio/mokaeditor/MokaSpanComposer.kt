package com.luckyhan.studio.mokaeditor

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ParagraphStyle
import android.util.Log
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import com.luckyhan.studio.mokaeditor.util.MokaTextUtil

class MokaSpanComposer {
    fun compose(dest: SpannableStringBuilder, src: MokaSpanCollector, start: Int, before: Int, after: Int) {
        val beforeStart = start
        val beforeEnd = beforeStart + before
        val afterStart = start
        val afterEnd = afterStart + after
        val gap = after - before

        val beforeText = src.text
        val spans = src.getSpans(0, beforeText.length, MokaSpan::class.java)
        spans.forEach { spanModel ->
            val spanStart = spanModel.start
            val spanEnd = spanModel.end
            val flag = spanModel.flag
            val span = spanModel.span
            if (beforeStart in spanStart..spanEnd) {
                if (span is ParagraphStyle) {
                    val lineStart = MokaTextUtil.getStartOfLine(dest.toString(), spanStart)
                    val lineEnd = MokaTextUtil.getEndOfLine(dest.toString(), spanStart)
                    if(beforeStart <= spanStart && gap < 0){
                        //remove
                    }else{
                        dest.setSpan(span, lineStart, lineEnd, flag)
                    }
                } else {
                    if (flag == Spannable.SPAN_INCLUSIVE_EXCLUSIVE) {
                        if(beforeStart <= spanStart || beforeStart < spanEnd){
                            if(spanStart < spanEnd+gap)
                                dest.setSpan(span, spanStart, spanEnd+gap, flag)
                        }else{
                            if(spanStart < beforeStart)
                                dest.setSpan(span, spanStart, beforeStart, flag)
                        }
                    } else if (flag == Spannable.SPAN_EXCLUSIVE_INCLUSIVE) {
                        if(beforeStart <= spanStart){
                            if(spanStart+gap < (spanEnd + gap))
                                dest.setSpan(span, spanStart+gap, spanEnd + gap, flag)
                        }else{
                            if(spanStart < (spanEnd + gap))
                                dest.setSpan(span, spanStart, spanEnd + gap, flag)
                        }
                    } else {
                        if (spanStart <= (spanEnd + gap))
                            dest.setSpan(span, spanStart, spanEnd + gap, flag)
                    }
                }
            } else if (beforeStart < spanStart && beforeEnd <= spanStart) {
                if (span is ParagraphStyle) {
                    val lineStart = MokaTextUtil.getStartOfLine(dest.toString(), spanStart+gap)
                    val lineEnd = MokaTextUtil.getEndOfLine(dest.toString(), spanStart+gap)
                    dest.setSpan(span, lineStart, lineEnd, flag)
                } else {
                    dest.setSpan(span, spanStart + gap, spanEnd + gap, flag)
                }
            } else if (beforeStart > spanEnd) {
                if (span is ParagraphStyle) {
                    val lineStart = MokaTextUtil.getStartOfLine(dest.toString(), spanStart)
                    val lineEnd = MokaTextUtil.getEndOfLine(dest.toString(), spanStart)
                    dest.setSpan(span, lineStart, lineEnd, flag)
                } else {
                    dest.setSpan(span, spanStart, spanEnd, flag)
                }
            } else if (beforeStart < spanStart && beforeEnd >= spanEnd) {
                // remove span
            } else if (spanStart in (beforeStart + 1) until beforeEnd && beforeEnd < spanEnd) {
                if (span is ParagraphStyle) {
                    //remove span
                } else {
                    dest.setSpan(span, afterEnd, spanEnd + gap, flag)
                }
            }
        }
    }
}