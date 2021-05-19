package com.luckyhan.studio.richeditor

import android.content.Context
import android.text.*
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText
import com.luckyhan.studio.richeditor.span.character.RichBoldSpan
import com.luckyhan.studio.richeditor.span.paragraph.CheckBoxSpan
import com.luckyhan.studio.richeditor.span.paragraph.RichBulletSpan
import kotlin.math.roundToInt

class RichEditText : AppCompatEditText {
    var isTouched = false
    var beforeSelection : Selection? = null

    val textWatcher = object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeSelection = Selection(this@RichEditText, start, start+count)
            beforeSelection?.onBeforeTextChanged()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            beforeSelection?.onAfterTextChanged(start, before, count)
        }

        override fun afterTextChanged(s: Editable?) {
            beforeSelection = null
        }
    }

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
        addTextChangedListener(textWatcher)
    }

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    override fun isSuggestionsEnabled(): Boolean {
        return false
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection {
        return RichEditorInputConnection(super.onCreateInputConnection(outAttrs), true)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
    }

    fun addBold() {
        val selection = Selection(this, selectionStart, selectionEnd)
        val isBold = selection.isThereSpan(RichBoldSpan::class.java)
        if(isBold){
            selection.removeCharacterSpan(RichBoldSpan::class.java)
        }else{
            val span = RichBoldSpan()
            selection.setCharacterSpan(span)
        }
    }

    fun addBullet() {
        val selection = Selection(this, selectionStart, selectionEnd)
        val isBullet = selection.isThereSpan(RichBulletSpan::class.java)
        if(isBullet){
            selection.removeParagraphSpan(RichBulletSpan::class.java)
        }else{
            val span = RichBulletSpan()
            selection.setParagraphSpan(span)
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