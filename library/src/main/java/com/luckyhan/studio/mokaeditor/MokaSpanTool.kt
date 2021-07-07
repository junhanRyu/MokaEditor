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
import com.luckyhan.studio.mokaeditor.span.MokaSpan
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

    fun switchSpan(span: MokaSpan) {
        when (span) {
            is CharacterStyle -> {
                val isExistSpan = isThereSpan(span.javaClass)
                if (isExistSpan) {
                    removeCharacterSpan(span.javaClass)
                } else {
                    addCharacterSpan(span)
                }
            }
            is ParagraphStyle -> {
                val isExistSpan = isThereSpan(span.javaClass)
                if (isExistSpan) {
                    removeParagraphSpan(span.javaClass)
                } else {
                    addParagraphSpan(span)
                }
            }
            else -> {
                throw UnsupportedOperationException("Span has to be either CharacterStyle or ParagraphStyle")
            }
        }
    }

    fun replaceSpan(span: MokaSpan) {
        when (span) {
            is CharacterStyle -> {
                val isExistSpan = isThereSpan(span.javaClass)
                if (isExistSpan) {
                    removeCharacterSpan(span.javaClass)
                }
                addCharacterSpan(span)
            }
            is ParagraphStyle -> {
                val isExistSpan = isThereSpan(span.javaClass)
                if (isExistSpan) {
                    removeParagraphSpan(span.javaClass)
                }
                addParagraphSpan(span)
            }
            else -> {
                throw UnsupportedOperationException("Span has to be either CharacterStyle or ParagraphStyle")
            }
        }
    }

    fun <T> getSpan(spanType: Class<T>): T? {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)

        if (spans?.isNotEmpty() == true) {
            return spans[0]
        }

        return null
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