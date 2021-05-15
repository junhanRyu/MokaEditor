package com.luckyhan.studio.richeditor

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.text.style.ParagraphStyle
import android.util.Log
import android.widget.EditText
import com.luckyhan.studio.richeditor.span.RichSpannable
import kotlin.IllegalStateException

class Selection(private val editText: EditText, private var selectionStart: Int, private var selectionEnd: Int) {
    private val zeroWidthChar = "\u200B"
    private var previousText : String? = null
    private val spannable = editText.text
    private val paragraphs: ArrayList<Paragraph> = ArrayList()
    private val characterSpans: ArrayList<SpanModel> = ArrayList()

    fun onBeforeTextChanged() {
        previousText = spannable.toString()
    }

    fun onAfterTextChanged() {

    }

    fun <T>isThereSpan(spanType : Class<T>) : Boolean{
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
            } else {
                throw IllegalStateException("span is not RichSpannable!")
            }
        }
    }

    private fun restoreCharacterSpans() {
        if(previousText == null){
            throw IllegalStateException("Spans are not stored before!")
        }else{

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

    private fun restoreParagraphs(){
        if(previousText == null){
            throw IllegalStateException("Spans are not stored before!")
        }else{

        }
    }

    private fun removeAllSpans() {
        spannable.clearSpans()
    }

    fun setCharacterSpan(span: CharacterStyle) {
        spannable.setSpan(span, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    fun <T> removeCharacterSpan(type: Class<T>) {
        val spans = spannable.getSpans(selectionStart, selectionEnd, type)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)

            if (selectionStart in (spanStart + 1) .. spanEnd && spanEnd in selectionStart .. selectionEnd) { // [ span { ] selection }
                spannable.removeSpan(span)
                spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            } else if (selectionEnd in (spanStart + 1) until spanEnd && spanStart in selectionStart until selectionEnd) { // { selection [} span ]
                spannable.removeSpan(span)
                spannable.setSpan(span, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            } else if (spanStart in selectionStart .. selectionEnd && spanEnd in selectionStart .. selectionEnd) {   // { selection [span] }
                spannable.removeSpan(span)
            } else if (selectionStart in (spanStart+1) until spanEnd && selectionEnd in (spanStart+1) until spanEnd) { // { span [selection] }
                if(span is RichSpannable){
                    val otherSpan = span.copy()
                    spannable.removeSpan(span)
                    spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(otherSpan, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                }else{
                    Log.e("selection", "selectionStart : $selectionStart, selectionEnd : $selectionEnd, spanStart : $spanStart, spanEnd : $spanEnd")
                    throw IllegalStateException("this span is not supported!")
                }
            } else {
                Log.e("selection", "selectionStart : $selectionStart, selectionEnd : $selectionEnd, spanStart : $spanStart, spanEnd : $spanEnd")
                throw IllegalStateException("Out of ranges!")
            }
        }
    }

    fun setParagraphSpan(span : RichSpannable){
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
                if(spannable is SpannableStringBuilder){
                    spannable.insert(start, zeroWidthChar)
                    spannable.setSpan(otherSpan, start, end+1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    start++
                }
                start += line.length + 1
                end = start
            }
            setTextWatcher()
        }
    }

    fun <T>removeParagraphSpan(spanType : Class<T>){
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        //TextWatcher has to be removed from here
        removeTextWatcher()
        for(span in spans){
            if(spannable is SpannableStringBuilder){
                val spanStart = spannable.getSpanStart(span)
                spannable.removeSpan(span)
                //debug
                if(spannable.substring(spanStart, spanStart+1) == zeroWidthChar){
                    Log.d("selection", "zero width there!")
                }
                spannable.delete(spanStart, spanStart+1)
            }
        }
        setTextWatcher()
    }

    private fun removeTextWatcher(){
        if(editText is TextWatcher){
            editText.removeTextChangedListener(editText)
        }
    }

    private fun setTextWatcher(){
        if(editText is TextWatcher){
            editText.addTextChangedListener(editText)
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