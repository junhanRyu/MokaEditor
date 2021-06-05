package com.luckyhan.studio.richeditor

import android.text.Editable
import android.text.Spannable
import android.text.style.QuoteSpan
import android.text.style.UnderlineSpan
import android.util.Log
import com.luckyhan.studio.richeditor.span.RichSpan
import com.luckyhan.studio.richeditor.span.character.*
import com.luckyhan.studio.richeditor.span.paragraph.RichBulletSpan
import com.luckyhan.studio.richeditor.span.paragraph.RichCheckBoxSpan
import com.luckyhan.studio.richeditor.span.paragraph.RichQuoteSpan
import com.luckyhan.studio.richeditor.util.RichTextUtil
import java.lang.NullPointerException

class RichSpanTool(private val editText: RichEditText) : RichEditText.RichSelectionChangeListener {

    private val spannable: Editable = editText.text?: throw NullPointerException("Editable is null")
    private val context = editText.context
    var toolsStateChangeListener : RichToolsStateChangeListener? = null

    interface RichToolsStateChangeListener{
        fun onToolsStateChanged()
    }

    init{
        editText.selectionChangeListenr = this
    }

    fun toggleStrikethrough() {
        val isStrikethrough = isThereSpan(RichStrikethroughSpan::class.java)
        if(isStrikethrough){
            removeCharacterSpan(RichStrikethroughSpan::class.java)
        }else{
            val span = RichStrikethroughSpan()
            addCharacterSpan(span)
        }
    }

    fun toggleUnderline() {
        val isUnderline = isThereSpan(RichUnderlineSpan::class.java)
        if(isUnderline){
            removeCharacterSpan(RichUnderlineSpan::class.java)
        }else{
            val span = RichUnderlineSpan()
            addCharacterSpan(span)
        }
    }

    fun setFontSize(size : Float) {
        val isColor = isThereSpan(RichFontSizeSpan::class.java)
        if(isColor){
            removeCharacterSpan(RichFontSizeSpan::class.java)
        }
        if(size != 1.0f){
            val span = RichFontSizeSpan(size)
            addCharacterSpan(span)
        }
    }

    fun setBackgroundColor(color : Int) {
        val isColor = isThereSpan(RichBackgroundColorSpan::class.java)
        if(isColor){
            removeCharacterSpan(RichBackgroundColorSpan::class.java)
        }
        val defaultColor = false
        if(!defaultColor){
            val span = RichBackgroundColorSpan(color)
            addCharacterSpan(span)
        }
    }

    fun setForegroundColor(color : Int) {
        val isColor = isThereSpan(RichForegroundColorSpan::class.java)
        if(isColor){
            removeCharacterSpan(RichForegroundColorSpan::class.java)
        }
        val defaultColor = false
        if(!defaultColor){
            val span = RichForegroundColorSpan(color)
            addCharacterSpan(span)
        }
    }

    fun toggleBold() {
        val isBold = isThereSpan(RichBoldSpan::class.java)
        if(isBold){
            removeCharacterSpan(RichBoldSpan::class.java)
        }else{
            val span = RichBoldSpan()
            addCharacterSpan(span)
        }
    }

    fun toggleQuote() {
        val isQuote = isThereSpan(RichQuoteSpan::class.java)
        if(isQuote){
            removeParagraphSpan(RichQuoteSpan::class.java)
        }else{
            val span = RichQuoteSpan()
            addParagraphSpan(span)
        }
    }

    fun toggleBullet() {
        val isBullet = isThereSpan(RichBulletSpan::class.java)
        if(isBullet){
            removeParagraphSpan(RichBulletSpan::class.java)
        }else{
            val span = RichBulletSpan()
            addParagraphSpan(span)
        }
    }

    fun toggleCheckBox() {
        val isCheckBox = isThereSpan(RichCheckBoxSpan::class.java)
        if(isCheckBox){
            removeParagraphSpan(RichCheckBoxSpan::class.java)
        }else{
            val span = RichCheckBoxSpan(context, spannable)
            addParagraphSpan(span)
        }
    }

    private fun <T> isThereSpan(spanType: Class<T>): Boolean {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        return spans.isNotEmpty()
    }

    private fun addCharacterSpan(span: RichSpan) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        spannable.setSpan(span, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    private fun <T> removeCharacterSpan(classType: Class<T>) {
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

    private fun addParagraphSpan(span: RichSpan) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val lineStart = RichTextUtil.getStartOfLine(spannable.toString(), selectionStart)
        val lineEnd = RichTextUtil.getEndOfLine(spannable.toString(), selectionEnd)
        val subString = spannable.toString().substring(lineStart, lineEnd)
        val lines = subString.split('\n')
        var start = lineStart
        var end = lineStart
        Log.d(this.javaClass.name, "line start : $lineStart, line end : $lineEnd")
        Log.d(this.javaClass.name, "length : ${spannable.toString().length}")
        Log.d(this.javaClass.name, "lines : ${lines.size}")
        for (line in lines) {
            Log.d(this.javaClass.name, "line : $line")
            val otherSpan = span.copy()
            end += line.length
            if(end < spannable.toString().length)
                end++// add offset for new line
            spannable.setSpan(otherSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            start += line.length + 1
            end = start
        }
    }

    private fun <T> removeParagraphSpan(classType: Class<T>) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, classType)
        for (span in spans) {
            spannable.removeSpan(span)
        }
    }

    override fun onSelectionChanged() {
        toolsStateChangeListener?.onToolsStateChanged()
    }
}