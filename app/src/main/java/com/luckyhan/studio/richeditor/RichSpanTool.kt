package com.luckyhan.studio.richeditor

import android.text.Editable
import android.text.Spannable
import android.widget.EditText
import com.luckyhan.studio.richeditor.span.RichSpan

class RichSpanTool(private val editText: EditText) {

    private val spannable: Editable = editText.text

    fun <T> isThereSpan(spanType: Class<T>): Boolean {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        return spans.isNotEmpty()
    }

    fun addCharacterSpan(span: RichSpan) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        spannable.setSpan(span, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    fun <T> removeCharacterSpan(classType: Class<T>) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, classType)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            if (selectionStart <= spanStart && selectionEnd >= spanEnd) {
                //remove
            } else if (selectionStart <= spanStart && selectionEnd < spanEnd) {
                spannable.setSpan(span, selectionEnd, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            } else if (selectionEnd >= spanEnd && selectionStart > spanStart) {
                spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            } else if (selectionStart > spanStart && selectionEnd < spanEnd) {
                if (span is RichSpan) {
                    val other = span.copy()
                    spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(other, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                }
            }
        }
    }

    fun addParagraphSpan(span: RichSpan) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val lineStart = RichTextUtils.getStartOfLine(spannable.toString(), selectionStart)
        val lineEnd = RichTextUtils.getEndOfLine(spannable.toString(), selectionEnd)
        val subString = spannable.toString().substring(lineStart, lineEnd)
        val lines = subString.split('\n')
        var start = lineStart
        var end = lineStart
        for (line in lines) {
            val otherSpan = span.copy()
            end += line.length
            if(end < spannable.toString().length)
                end++// add offset for new line
            spannable.setSpan(otherSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            start += line.length + 1
            end = start
        }
    }

    fun <T> removeParagraphSpan(classType: Class<T>) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, classType)
        for (span in spans) {
            spannable.removeSpan(span)
        }
    }
}