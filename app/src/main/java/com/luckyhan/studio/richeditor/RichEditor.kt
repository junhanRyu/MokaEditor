package com.luckyhan.studio.richeditor

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.text.style.*
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText
import kotlin.math.roundToInt

class RichEditor : AppCompatEditText {
    private val zeroWidthChar = "\u200B"
    //private val zeroWidthChar = "S"

    var isTouched = false
    var handleInWatcher = true

    private inner class RichEditorInputConnection(target: InputConnection, mutable: Boolean) :
        InputConnectionWrapper(target, mutable) {
        override fun sendKeyEvent(event: KeyEvent?): Boolean {
            event?.let {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_DEL -> {
                        }
                        KeyEvent.KEYCODE_ENTER -> {
                        }
                    }
                }
            }
            return super.sendKeyEvent(event)
        }
    }

    init {
        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("span", "before = start : $start, count : $count")
                Log.d("span", text?.substring(start, start+count) ?: "")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("span", "after = start : $start, count : $count")
                Log.d("span", text?.substring(start, start+count) ?: "")
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        addTextChangedListener(textWatcher)
    }

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection {
        return RichEditorInputConnection(super.onCreateInputConnection(outAttrs), true)
    }

    private fun getStartOfLine(offset: Int): Int {
        val text = text.toString()
        if (offset < 0) return -1
        if (text.isEmpty()) return 0
        var start = offset
        while (start > 0) {
            if (text[start - 1] == '\n') break
            start--
        }
        return start
    }

    private fun getEndOfLine(offset: Int): Int {
        val text = text.toString()
        if (offset < 0) return -1
        if (text.isEmpty()) return 0
        var end = offset
        while (end < text.length) {
            if (text[end] == '\n') break
            end++
        }
        return end
    }

    private fun addParagraphSpan(span: ParagraphStyle, offset: Int): Boolean {
        val lineStart = getStartOfLine(offset)
        val lineEnd = getEndOfLine(offset)
        handleInWatcher = false
        text?.insert(lineStart, zeroWidthChar)
        text?.setSpan(span, lineStart, lineEnd + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        return true
    }


    private fun <T> removeParagraphSpan(spanType: Class<T>, offset: Int): Boolean {
        val lineStart = getStartOfLine(offset)
        val lineEnd = getEndOfLine(offset)
        val spans = text?.getSpans(lineStart, lineEnd, spanType)
        spans?.let {
            for (span in spans) {
                text?.removeSpan(span)
            }
            val firstChar = text?.substring(lineStart, lineStart + 1)
            if (firstChar == zeroWidthChar) {
                handleInWatcher = false
                text?.delete(lineStart, lineStart + 1)
                return true
            }
        }
        return false
    }

    private fun getLines(start: Int, end: Int): List<String>? {
        val subString = text?.substring(start, end)
        return subString?.split("\n")
    }

    // true : add
    // false : remove
    private fun <T> getChoiceToAddParagraphSpan(spanType: Class<T>): Boolean {
        val lineStart = getStartOfLine(selectionStart)
        val lineEnd = getEndOfLine(selectionEnd)
        val lines = getLines(lineStart, lineEnd)
        lines?.let {
            if (lines.isNotEmpty()) {
                val firstLineStart = lineStart
                val firstLineEnd = lineStart + lines[0].length
                val spans = text?.getSpans(firstLineStart, firstLineEnd, spanType)
                if (spans?.isNotEmpty() == true) {
                    return false
                }
            }
        }
        return true
    }

    private fun <T> addParagraphSpans(spanType: Class<T>) {
        val lineStart = getStartOfLine(selectionStart)
        val lineEnd = getEndOfLine(selectionEnd)
        val lines = getLines(lineStart, lineEnd)
        if (lines?.isNotEmpty() == true) {
            var startOffset = lineStart
            var endOffset = lineStart
            for (line in lines) {
                endOffset += line.length
                val spans = text?.getSpans(startOffset, endOffset, spanType)
                var zeroWidthCharAdded = false
                if (spans?.isEmpty() == true) {
                    val span = spanType.constructors.first().newInstance() as ParagraphStyle
                    zeroWidthCharAdded = addParagraphSpan(span, startOffset)
                }
                //add offset for new line character
                startOffset += line.length + 1
                if (zeroWidthCharAdded) startOffset += 1
                endOffset = startOffset

            }
        }
    }

    private fun <T> removeParagraphSpans(spanType: Class<T>) {
        val lineStart = getStartOfLine(selectionStart)
        val lineEnd = getEndOfLine(selectionEnd)
        val lines = getLines(lineStart, lineEnd)
        if (lines?.isNotEmpty() == true) {
            var startOffset = lineStart
            var endOffset = lineStart
            for (line in lines) {
                endOffset += line.length
                var zeroWidthCharRemoved = false
                val spans = text?.getSpans(startOffset, endOffset, spanType)
                if (spans?.isNotEmpty() == true) {
                    zeroWidthCharRemoved = removeParagraphSpan(spanType, startOffset)
                }
                //add offset for new line character
                startOffset += line.length + 1
                if (zeroWidthCharRemoved) startOffset -= 1
                endOffset = startOffset
            }
        }
    }

    private fun addCharacterSpan(span: CharacterStyle) {
        text?.setSpan(span, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    private fun removeCharacterSpans(spans: List<CharacterStyle>) {
        for (span in spans) {
            val spanStart = text?.getSpanStart(span) ?: 0
            val spanEnd = text?.getSpanEnd(span) ?: 0
            if (spanStart < selectionStart && spanEnd <= selectionEnd) {
                text?.removeSpan(span)
                text?.setSpan(span, spanStart, selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else if (spanStart >= selectionStart && spanEnd > selectionEnd) {
                text?.removeSpan(span)
                text?.setSpan(span, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            } else if (spanStart >= selectionStart && spanEnd <= selectionEnd) {
                text?.removeSpan(span)
            } else if (spanStart < selectionStart && spanEnd > selectionEnd) {
                //val rightSpan = span.
                text?.removeSpan(span)
                text?.setSpan(span, spanStart, selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val otherSpan = span.copy()
                text?.setSpan(otherSpan, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }
    }

    private fun CharacterStyle.copy() : StyleSpan{
        this as StyleSpan
        return StyleSpan(this.style)
    }

    fun addBold() {
        val spans = text?.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
        if (spans?.isNotEmpty() == true) {
            val boldSpans = spans.filter { it.style == Typeface.BOLD }
            removeCharacterSpans(boldSpans)
        } else {
            addCharacterSpan(StyleSpan(Typeface.BOLD))
        }
    }

    fun addBullet() {
        val choiceToAdd = getChoiceToAddParagraphSpan(BulletSpan::class.java)
        if (choiceToAdd) {
            addParagraphSpans(BulletSpan::class.java)
        } else {
            removeParagraphSpans(BulletSpan::class.java)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                if (isTouched) {
                    isTouched = false
                    val checkBoxSpan = isChecked(event.x, event.y)
                    if (checkBoxSpan != null) {
                        toggleCheckBox(checkBoxSpan)
                        return true
                    }
                }
                return super.onTouchEvent(event)
            }
            MotionEvent.ACTION_DOWN -> {
                isTouched = true
                val checkBoxSpan = isChecked(event.x, event.y)
                if (checkBoxSpan != null)
                    return true
                return super.onTouchEvent(event)
            }
            else -> {
                isTouched = false
                return super.onTouchEvent(event)
            }
        }
    }

    private fun isChecked(x: Float, y: Float): CheckBoxSpan? {
        val padding: Int = totalPaddingTop + totalPaddingBottom
        val areaTop: Int = scrollY + y.roundToInt() - totalPaddingTop
        val areaBot: Int = areaTop + height - padding

        val lineTop = layout.getLineForVertical(areaTop)
        val lineBot = layout.getLineForVertical(areaBot)

        val first = layout.getLineStart(lineTop)
        val end = layout.getLineEnd(lineTop)
        val clickedCharOffset = layout.getOffsetForHorizontal(lineTop, (x - paddingStart))

        val spans = text?.getSpans(first, end, CheckBoxSpan::class.java)
        spans?.let {
            for (span in spans) {
                if (span.isChecked(
                        x.roundToInt() - totalPaddingLeft,
                        y.roundToInt() - totalPaddingTop
                    )
                ) {
                    return span
                }
            }
        }
        return null
    }

    private fun isCheckBox(start: Int, end: Int): Boolean {
        val spans = text?.getSpans(start, end, CheckBoxSpan::class.java)
        return ((spans?.size ?: 0) > 0)
    }

    fun toggleCheckBox(span: CheckBoxSpan) {
        val start = text?.getSpanStart(span) ?: -1
        val end = text?.getSpanEnd(span) ?: -1
        if (start != -1 && end != -1) {
            val checked = !span.checked
            val newSpan = CheckBoxSpan(context, checked)
            text?.setSpan(newSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            text?.removeSpan(span)
        }
    }

    fun addCheckBox() {
        var start = selectionStart
        val end = selectionEnd
        val lines = text?.split("\n")
        lines?.let {
            var lineStart = 0
            var lineEnd = 0
            for (line in lines.indices) {
                lineEnd += lines[line].length
                Log.d("span", "lineStart : ${lineStart}, lineEnd : ${lineEnd}")
                Log.d("span", "start : ${start}, end : ${end}")
                if (lineStart > end) break
                if (lineEnd < start) {
                    lineStart += lines[line].length + 1
                    lineEnd += 1
                    continue
                }
                if (line != lines.size - 1) {
                    lineEnd += 1
                }
                if (isCheckBox(lineStart, lineEnd)) continue
                val newSpan = CheckBoxSpan(context, false)
                text?.setSpan(newSpan, lineStart, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                lineStart += lines[line].length + 1

            }
        }
        Log.d("span", "lines : ${lines?.size}")
        /*while (start <= end) {
            val line = layout.getLineForOffset(start)
            val lineStart = layout.getLineStart(line)
            val lineEnd = layout.getLineEnd(line)
            Log.d("span", "line: ${line}, lineStart : ${lineStart}, lineEnd : ${lineEnd}")
            start = lineEnd + 1
            if (isCheckBox(lineStart, lineEnd)) {
                continue
            }
            val newSpan = CheckBoxSpan(context)
            text?.setSpan(newSpan, lineStart, lineEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }*/
    }

    fun removeCheckBox() {
        var start = selectionStart
        val end = selectionEnd
        while (start <= end) {
            val line = layout.getLineForOffset(start)
            val lineStart = layout.getLineStart(line)
            val lineEnd = layout.getLineEnd(line)
            start = lineEnd + 1
            if (!isCheckBox(lineStart, lineEnd)) {
                continue
            }
            val spans = text?.getSpans(lineStart, lineEnd, CheckBoxSpan::class.java)
            spans?.let {
                for (span in spans) {
                    text?.removeSpan(span)
                }
            }
        }
    }


}