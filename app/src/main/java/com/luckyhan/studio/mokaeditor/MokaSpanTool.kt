package com.luckyhan.studio.mokaeditor

import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.util.Log
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import com.luckyhan.studio.mokaeditor.span.character.*
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaBulletSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaCheckBoxSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaQuoteSpan
import com.luckyhan.studio.mokaeditor.util.MokaTextUtil
import java.lang.NullPointerException

class MokaSpanTool(private val editText: MokaEditText) : MokaEditText.RichSelectionChangeListener {

    private val spannable: Editable = editText.text?: throw NullPointerException("Editable is null")
    private val context = editText.context
    private var toolsStateChangeListener : RichToolsStateChangeListener? = null

    var bulleted : Boolean = false
        private set
    var underlined : Boolean = false
        private set
    var strikethroughed : Boolean = false
        private set
    var foregroundColored : Int = editText.currentTextColor
        private set
    var backgroundColored : Int = Color.TRANSPARENT
        private set
    var checkboxed : Boolean = false
        private set
    var quoted : Boolean = false
        private set
    var bolded : Boolean = false
        private set
    var textSized : Float = 1.0f
        private set

    interface RichToolsStateChangeListener{
        fun onToolsStateChanged()
    }

    init{
        editText.selectionChangeListenr = this
    }

    private fun updateToolStates(){
        bulleted = isThereSpan(MokaBulletSpan::class.java)
        underlined = isThereSpan(MokaUnderlineSpan::class.java)
        quoted = isThereSpan(MokaQuoteSpan::class.java)
        bolded = isThereSpan(MokaBoldSpan::class.java)
        checkboxed = isThereSpan(MokaCheckBoxSpan::class.java)
        strikethroughed = isThereSpan(MokaStrikethroughSpan::class.java)
        strikethroughed = isThereSpan(MokaBulletSpan::class.java)

        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd

        var foregroundSpans = spannable.getSpans(selectionStart, selectionEnd, MokaForegroundColorSpan::class.java)
        if(foregroundSpans.isNotEmpty()){
            foregroundColored = foregroundSpans[0].color
        }

        var backgroundSpans = spannable.getSpans(selectionStart, selectionEnd, MokaBackgroundColorSpan::class.java)
        if(backgroundSpans.isNotEmpty()){
            backgroundColored = backgroundSpans[0].color
        }

        var fontSizeSpans = spannable.getSpans(selectionStart, selectionEnd, MokaFontSizeSpan::class.java)
        if(fontSizeSpans.isNotEmpty()){
            textSized = fontSizeSpans[0].proportion
        }

        Log.d(this.javaClass.name, "bold : $bolded size : $textSized bullet : $bulleted")
    }

    fun toggleStrikethrough() {
        val isStrikethrough = isThereSpan(MokaStrikethroughSpan::class.java)
        if(isStrikethrough){
            removeCharacterSpan(MokaStrikethroughSpan::class.java)
        }else{
            val span = MokaStrikethroughSpan()
            addCharacterSpan(span)
        }
    }

    fun toggleUnderline() {
        val isUnderline = isThereSpan(MokaUnderlineSpan::class.java)
        if(isUnderline){
            removeCharacterSpan(MokaUnderlineSpan::class.java)
        }else{
            val span = MokaUnderlineSpan()
            addCharacterSpan(span)
        }
    }

    fun setFontSize(size : Float) {
        val isColor = isThereSpan(MokaFontSizeSpan::class.java)
        if(isColor){
            removeCharacterSpan(MokaFontSizeSpan::class.java)
        }
        if(size != 1.0f){
            val span = MokaFontSizeSpan(size)
            addCharacterSpan(span)
        }
    }

    fun setBackgroundColor(color : Int) {
        val isColor = isThereSpan(MokaBackgroundColorSpan::class.java)
        if(isColor){
            removeCharacterSpan(MokaBackgroundColorSpan::class.java)
        }
        if(color != Color.TRANSPARENT){
            val span = MokaBackgroundColorSpan(color)
            addCharacterSpan(span)
        }
    }

    fun setForegroundColor(color : Int) {
        val isColor = isThereSpan(MokaForegroundColorSpan::class.java)
        if(isColor){
            removeCharacterSpan(MokaForegroundColorSpan::class.java)
        }
        val defaultColor = editText.currentTextColor
        if(color != defaultColor){
            val span = MokaForegroundColorSpan(color)
            addCharacterSpan(span)
        }
    }

    fun toggleBold() {
        val isBold = isThereSpan(MokaBoldSpan::class.java)
        if(isBold){
            removeCharacterSpan(MokaBoldSpan::class.java)
        }else{
            val span = MokaBoldSpan()
            addCharacterSpan(span)
        }
    }

    fun toggleQuote() {
        val isQuote = isThereSpan(MokaQuoteSpan::class.java)
        if(isQuote){
            removeParagraphSpan(MokaQuoteSpan::class.java)
        }else{
            val span = MokaQuoteSpan()
            addParagraphSpan(span)
        }
    }

    fun toggleBullet() {
        val isBullet = isThereSpan(MokaBulletSpan::class.java)
        if(isBullet){
            removeParagraphSpan(MokaBulletSpan::class.java)
        }else{
            val span = MokaBulletSpan()
            addParagraphSpan(span)
        }
    }

    fun toggleCheckBox() {
        val isCheckBox = isThereSpan(MokaCheckBoxSpan::class.java)
        if(isCheckBox){
            removeParagraphSpan(MokaCheckBoxSpan::class.java)
        }else{
            val span = MokaCheckBoxSpan(context, spannable)
            addParagraphSpan(span)
        }
    }

    private fun <T> isThereSpan(spanType: Class<T>): Boolean {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        return spans.isNotEmpty()
    }

    private fun addCharacterSpan(span: MokaSpan) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        spannable.setSpan(span, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        updateToolStates()
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
                if (span is MokaSpan) {
                    val other = span.copy()
                    spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(other, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                }
            }
        }
        updateToolStates()
    }

    private fun addParagraphSpan(span: MokaSpan) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val lineStart = MokaTextUtil.getStartOfLine(spannable.toString(), selectionStart)
        val lineEnd = MokaTextUtil.getEndOfLine(spannable.toString(), selectionEnd)
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
        updateToolStates()
    }

    private fun <T> removeParagraphSpan(classType: Class<T>) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, classType)
        for (span in spans) {
            spannable.removeSpan(span)
        }
        updateToolStates()
    }

    override fun onSelectionChanged() {
        updateToolStates()
        toolsStateChangeListener?.onToolsStateChanged()
    }
}