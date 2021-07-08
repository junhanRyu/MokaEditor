package com.luckyhan.studio.mokaeditor

import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.CharacterStyle
import android.text.style.LeadingMarginSpan
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2
import android.text.style.ParagraphStyle
import android.util.Log
import com.luckyhan.studio.mokaeditor.span.MokaCharacterStyle
import com.luckyhan.studio.mokaeditor.span.MokaParagraphStyle
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import com.luckyhan.studio.mokaeditor.span.SelectionMarkupSpan
import com.luckyhan.studio.mokaeditor.util.MokaTextUtil

class MokaSpanTool(private val editText: MokaEditText, private val parser: MokaSpanParser) : MokaEditText.SelectionChangeListener,
    MokaEditText.TextChangeListener {

    private val spannable = editText.text as SpannableStringBuilder
    private var toolStateChangeListener: SpanToolStateChangeListener? = null
    private val redoUndoStack: ArrayList<String> = ArrayList()
    private var stackCursor = -1

    var redoable: Boolean = false
        private set
    var undoable: Boolean = false
        private set

    interface SpanToolStateChangeListener {
        fun onToolsStateChanged()
    }

    fun setSpanToolStateChangeListener(listener: SpanToolStateChangeListener) {
        toolStateChangeListener = listener
    }

    init {
        editText.selectionChangeListenr = this
        editText.textChangeListener = this

        val currentText = parser.getString(spannable)
        redoUndoStack.add(currentText)
        stackCursor = 0
    }


    private fun updateToolStates() {
        if (redoUndoStack.isNotEmpty() && stackCursor >= 0) {
            undoable = true
        }

        if (redoUndoStack.isNotEmpty() && redoUndoStack.size > stackCursor + 1) {
            redoable = true
        }
        toolStateChangeListener?.onToolsStateChanged()
        Log.d(this.javaClass.name, parser.getString(spannable))
    }

    //toggle spans
    fun toggleParagraphStyleSpan(span: MokaSpan) {
        if(span !is MokaParagraphStyle) return
        val selectionMarkup = SelectionMarkupSpan()
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        spannable.setSpan(selectionMarkup, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        val paragraphSpans = spannable.getSpans(selectionStart, selectionEnd, MokaParagraphStyle::class.java)
        val thatSpans = spannable.getSpans(selectionStart, selectionEnd, span.javaClass)
        val requestRemove = paragraphSpans.isNotEmpty()
        val requestSet = thatSpans.isEmpty()
        if(requestRemove) removeParagraphSpan(MokaParagraphStyle::class.java, selectionStart, selectionEnd)
        val spanStart = spannable.getSpanStart(selectionMarkup)
        val spanEnd = spannable.getSpanEnd(selectionMarkup)
        if(requestSet) addParagraphSpan(span, spanStart, spanEnd)
        spannable.removeSpan(selectionMarkup)
    }

    fun toggleCharacterStyleSpan(span : MokaSpan){
        if(span !is MokaCharacterStyle) return
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val isExistSpan = isThereSpan(span.javaClass)
        if (isExistSpan) {
            removeCharacterSpan(span.javaClass, selectionStart, selectionEnd)
        } else {
            addCharacterSpan(span)
        }
    }

    fun replaceCharacterStyleSpan(span : MokaSpan){
        if(span !is MokaCharacterStyle) return
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        removeCharacterSpan(span.javaClass, selectionStart, selectionEnd)
        addCharacterSpan(span)
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

    private fun <T> removeCharacterSpan(classType: Class<T>, selectionStart : Int, selectionEnd : Int) {
        val spans = spannable.getSpans(selectionStart, selectionEnd, classType)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)

            if((spanStart in selectionStart..selectionEnd) && (spanEnd in selectionStart..selectionEnd)){
                //just remove span
            }else if((spanStart in selectionStart..selectionEnd) && (spanEnd > selectionEnd)){
                spannable.setSpan(span, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }else if((spanEnd in selectionStart..selectionEnd) && (spanStart < selectionStart)){
                spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }else if((spanStart < selectionStart && selectionStart < spanEnd-1) && (selectionEnd in (spanStart + 1) until spanEnd)){
                if (span is MokaSpan) {
                    val otherSpan = span.copy()
                    spannable.setSpan(span, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    spannable.setSpan(otherSpan, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }else{
                throw UnsupportedOperationException("this span range is out of use cases!")
            }
        }
        updateToolStates()
    }

    private fun addParagraphSpan(span: MokaSpan, selectionStart : Int, selectionEnd : Int) {
        val lineStart = MokaTextUtil.getStartOfLine(spannable.toString(), selectionStart)
        val lineEnd = MokaTextUtil.getEndOfLine(spannable.toString(), selectionEnd)
        val subString = spannable.toString().substring(lineStart, lineEnd)
        val lines = subString.split('\n')
        var start = lineStart
        for (line in lines) {
            val otherSpan = span.copy()
            spannable.insert(start, MokaTextUtil.META_CHARACTER)
            val end = start + line.length + 1
            spannable.setSpan(otherSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            Log.d(MokaEditText.TAG, "start : $start, end : $end, text : ${spannable.substring(start, end)}")
            start = end + 1

        }
        updateToolStates()
    }

    private fun <T> removeParagraphSpan(classType: Class<T>, selectionStart : Int, selectionEnd : Int) {
        val spans = spannable.getSpans(selectionStart, selectionEnd, classType)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            if(spanStart + 1 <= spannable.length && spannable.substring(spanStart, spanStart+1) == MokaTextUtil.META_CHARACTER){
                spannable.replace(spanStart, spanStart+1, "")
            }
        }
        updateToolStates()
    }

    override fun onSelectionChanged() {
        updateToolStates()
    }

    override fun onTextChanged() {
        val current = parser.getString(spannable)
        while (stackCursor != redoUndoStack.size - 1) {
            redoUndoStack.removeAt(redoUndoStack.size - 1)
        }
        redoUndoStack.add(current)
        stackCursor = redoUndoStack.size - 1
    }

    fun redo() {
        if (redoUndoStack.isNotEmpty() && stackCursor + 1 < redoUndoStack.size) {
            stackCursor++
            val text = redoUndoStack[stackCursor]
            setSpansFromJson(text)
        }
    }

    fun undo() {
        if (redoUndoStack.isNotEmpty() && stackCursor - 1 >= 0) {
            stackCursor--
            val text = redoUndoStack[stackCursor]
            setSpansFromJson(text)
        }
    }

    private fun setSpansFromJson(jsonString: String) {
        val rawText = parser.getRawText(jsonString)
        editText.textWatcherEnabled = false
        editText.setText(rawText)
        editText.textWatcherEnabled = true
        parser.parseString(editText, jsonString)
        editText.setSelection(spannable.length)
    }
}