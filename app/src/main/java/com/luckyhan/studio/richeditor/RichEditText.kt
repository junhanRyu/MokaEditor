package com.luckyhan.studio.richeditor

import android.content.Context
import android.text.*
import android.util.AttributeSet
import android.util.Log
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

    private val textWatcher = object : TextWatcher {
        var spanCollector: RichSpanCollector? = null
        var start: Int = 0
        var before: Int = 0
        var after: Int = 0

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (s is Spannable) {
                spanCollector = RichSpanCollector()
                spanCollector?.collect(s)
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            this.start = start
            this.after = count
            this.before = before
        }

        override fun afterTextChanged(s: Editable?) {
            if (s is SpannableStringBuilder) {
                Log.d(this.javaClass.name, "watcher depth ${s.textWatcherDepth}")
                spanCollector?.let{
                    val composer = RichSpanComposer()
                    composer.compose(s, it, start, before, after)
                }
            }
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

    fun addBold() {
        val spanTool = RichSpanTool(this)
        val isBold = spanTool.isThereSpan(RichBoldSpan::class.java)
        if(isBold){
            spanTool.removeCharacterSpan(RichBoldSpan::class.java)
        }else{
            val span = RichBoldSpan()
            spanTool.addCharacterSpan(span)
        }
    }

    fun addBullet() {
        val spanTool = RichSpanTool(this)
        val isBullet = spanTool.isThereSpan(RichBulletSpan::class.java)
        if(isBullet){
            spanTool.removeParagraphSpan(RichBulletSpan::class.java)
        }else{
            val span = RichBulletSpan()
            spanTool.addParagraphSpan(span)
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