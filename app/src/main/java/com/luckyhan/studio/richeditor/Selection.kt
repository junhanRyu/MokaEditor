package com.luckyhan.studio.richeditor

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle
import android.text.style.ParagraphStyle
import android.util.Log
import android.widget.EditText
import com.luckyhan.studio.richeditor.span.RichSpannable
import kotlin.IllegalStateException

class Selection(private val editText: EditText, private var selectionStart: Int, private var selectionEnd: Int) {
    private var previousText: String? = null
    private val spannable = editText.text
    private val paragraphs: ArrayList<Paragraph> = ArrayList()
    private val characterSpans: ArrayList<SpanModel> = ArrayList()

    fun onBeforeTextChanged() {
        previousText = spannable.toString()
        storeCharacterSpans()
        storeParagraphs()
        removeAllStoredSpans()
    }

    fun onAfterTextChanged(start: Int, lengthBefore: Int, lengthAfter: Int) {
        removeTextWatcher()
        restoreCharacterSpans(start, lengthBefore, lengthAfter)
        setTextWatcher()
    }

    fun <T> isThereSpan(spanType: Class<T>): Boolean {
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        return spans.isNotEmpty()
    }

    private fun storeCharacterSpans() {
        val spans = spannable.getSpans(0, spannable.length, CharacterStyle::class.java)
        for (span in spans) {
            if (span is RichSpannable) {
                val spanStart = spannable.getSpanStart(span)
                val spanEnd = spannable.getSpanEnd(span)
                val spanFlag = spannable.getSpanFlags(span)
                characterSpans.add(SpanModel(span, spanFlag, spanStart, spanEnd))
            }
        }
    }

    private fun restoreCharacterSpans(start: Int, before: Int, after: Int) {
        if (previousText == null) {
            throw IllegalStateException("Spans are not stored before!")
        } else {
            val afterEnd = start + after
            val beforeEnd = start + before
            val offset = after - before
            Log.d("selection", "start : $start, before : $before, after : $after")
            Log.d("selection", "afterEnd : $afterEnd, beforeEnd : $beforeEnd, offset : $offset")
            for (spanModel in characterSpans) {
                val span = spanModel.span
                val spanStart = spanModel.start
                val spanEnd = spanModel.end
                val flag = spanModel.flag
                if(spanEnd < start){
                    spannable.setSpan(span, spanStart, spanEnd, flag)
                }else if((beforeEnd in spanStart until spanEnd && start <= spanStart)){
                    if(flag == Spannable.SPAN_INCLUSIVE_INCLUSIVE || flag == Spannable.SPAN_INCLUSIVE_EXCLUSIVE){
                        //stretch spans
                        spannable.setSpan(span, start, spanEnd+offset, flag)
                    }else{
                        spannable.setSpan(span, afterEnd, spanEnd+offset, flag)
                    }
                }else if(start in spanStart .. spanEnd && beforeEnd >= spanEnd){
                    if(flag == Spannable.SPAN_INCLUSIVE_INCLUSIVE || flag == Spannable.SPAN_EXCLUSIVE_INCLUSIVE){
                        //stretch spans
                        spannable.setSpan(span, spanStart, afterEnd, flag)
                    }else{
                        spannable.setSpan(span, spanStart, start, flag)
                    }
                }else if(spanStart < start && beforeEnd <= spanEnd){
                    //stretch spans
                    spannable.setSpan(span, spanStart, spanEnd+offset, flag)
                }else if(spanStart >= start && spanEnd <= beforeEnd){
                    //don't restore spans
                }else if(beforeEnd < spanStart){
                    spannable.setSpan(span, spanStart, spanEnd, flag)
                }else{
                    Log.e("selection", "spanStart : $spanStart, spanEnd : $spanEnd, textStart : $start, textEnd : $beforeEnd")
                    throw IllegalStateException("out of ranges!")
                }
            }
        }
    }

    private fun storeParagraphs() {
        val lineStart = getStartOfLine(spannable.toString(), 0)
        val lineEnd = getEndOfLine(spannable.toString(), spannable.toString().length)
        val subString = spannable.toString().substring(lineStart, lineEnd)
        if (subString.isNotEmpty()) {
            val lines = subString.split('\n')
            var start = lineStart
            var end = lineStart
            for (line in lines) {
                end += line.length
                val paragraph = Paragraph(start, end)
                paragraph.storeSpans(spannable)
                paragraphs.add(paragraph)
                start += end + 1
                end = start
            }
        }
    }

    private fun restoreParagraphs(start: Int, before: Int, after: Int) {
        if (previousText == null) {
            throw IllegalStateException("Spans are not stored before!")
        } else {
            val afterEnd = start + after
            val beforeEnd = start + before
            val offset = after - before

            if(offset == 1 && spannable.toString().substring(start, afterEnd) == "\n"){
                Log.d("selection", "new line!")
            }else if(offset == -1 && previousText?.substring(start, beforeEnd) == Paragraph.zeroWidthChar){
                Log.d("selection", "span deleted!")
            }else{
                for(paragraph in paragraphs){

                }
            }
        }
    }

    private fun removeAllStoredSpans() {
        for (span in characterSpans) {
            spannable.removeSpan(span)
        }
        removeTextWatcher()
        for (paragraph in paragraphs) {
            paragraph.removeSpans(spannable)
        }
        setTextWatcher()
    }

    fun setCharacterSpan(span: CharacterStyle) {
        spannable.setSpan(span, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    fun <T> removeCharacterSpan(type: Class<T>) {
        val spans = spannable.getSpans(selectionStart, selectionEnd, type)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            if(selectionEnd in spanStart until spanEnd && selectionStart <= spanStart){
                spannable.setSpan(span, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }else if(selectionStart in spanStart .. spanEnd && selectionEnd >= spanEnd){
                spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }else if(spanStart < selectionStart && selectionEnd <= spanEnd){
                if (span is RichSpannable) {
                    val oppositeSpan = span.copy()
                    spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(oppositeSpan, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                } else {
                    throw IllegalStateException("this span is not supported!")
                }
            }else if(spanStart >= selectionStart && spanEnd <= selectionEnd){
                //already done
            }
            else{
                throw IllegalStateException("out of ranges!")
            }
        }
    }

    fun setParagraphSpan(span: RichSpannable) {
        val lineStart = getStartOfLine(spannable.toString(), selectionStart)
        val lineEnd = getEndOfLine(spannable.toString(), selectionEnd)
        val subString = spannable.toString().substring(lineStart, lineEnd)
        if (subString.isNotEmpty()) {
            val lines = subString.split('\n')
            var start = lineStart
            var end = lineStart
            removeTextWatcher()
            for (line in lines) {
                val otherSpan = span.copy()
                end += line.length
                if (spannable is SpannableStringBuilder) {
                    spannable.insert(start, Paragraph.zeroWidthChar)
                    spannable.setSpan(otherSpan, start, end + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    start++
                }
                start += line.length + 1
                end = start
            }
            setTextWatcher()
        }
    }

    fun <T> removeParagraphSpan(spanType: Class<T>) {
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        //TextWatcher has to be removed from here
        removeTextWatcher()
        for (span in spans) {
            if (spannable is SpannableStringBuilder) {
                val spanStart = spannable.getSpanStart(span)
                spannable.removeSpan(span)
                //debug
                if (spannable.substring(spanStart, spanStart + 1) == Paragraph.zeroWidthChar) {
                    Log.d("selection", "zero width there!")
                }
                spannable.delete(spanStart, spanStart + 1)
            }
        }
        setTextWatcher()
    }

    private fun removeTextWatcher() {
        if (editText is RichEditText) {
            editText.removeTextChangedListener(editText.textWatcher)
        }
    }

    private fun setTextWatcher() {
        if (editText is RichEditText) {
            editText.addTextChangedListener(editText.textWatcher)
        }
    }

    private fun getStartOfLine(text: String, offset: Int): Int {
        if (offset < 0) throw IndexOutOfBoundsException("offset is negative!")
        if (text.isEmpty()) return 0
        var lineStart = offset
        while (lineStart > 0) {
            if (text[lineStart - 1] == '\n') break
            lineStart--
        }
        return lineStart
    }

    private fun getEndOfLine(text: String, offset: Int): Int {
        if (offset < 0) throw IndexOutOfBoundsException("offset is negative!")
        if (text.isEmpty()) return 0
        var lineEnd = offset
        while (lineEnd < text.length) {
            if (text[lineEnd] == '\n') break
            lineEnd++
        }
        return lineEnd
    }
}