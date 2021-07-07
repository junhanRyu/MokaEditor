package com.luckyhan.studio.mokaeditor

import android.content.Context
import android.text.*
import android.text.TextUtils.TruncateAt
import android.text.style.CharacterStyle
import android.text.style.ParagraphStyle
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.luckyhan.studio.mokaeditor.span.MokaClickable
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaStrikeThroughParagraphSpan
import com.luckyhan.studio.mokaeditor.util.MokaTextUtil
import kotlin.math.roundToInt


class MokaEditText : AppCompatEditText {

    companion object {
        val TAG = "MokaEditText"

    }

    var selectionChangeListenr: SelectionChangeListener? = null
    var textChangeListener: TextChangeListener? = null
    var textWatcherEnabled = true
    private val textWatcher = MokaTextWatcher()
    private val CLICK_THRESHOLD = 100

    interface SelectionChangeListener {
        fun onSelectionChanged()
    }

    interface TextChangeListener {
        fun onTextChanged()
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        val source = text.toString()

        if(selStart == selEnd && selStart < source.length && (source.isNotEmpty()) && source.substring(selStart, selStart+1) == MokaTextUtil.META_CHARACTER){
            Log.d(TAG, "selection adjust start : $selStart, end : $selEnd")
            setSelection(selStart+1)
        }else{
            super.onSelectionChanged(selStart, selEnd)
            selectionChangeListenr?.onSelectionChanged()
            Log.d(TAG, "selection start : $selStart, end : $selEnd")
        }
    }


    inner class MokaInputConnection(inputConnection: InputConnection?) : InputConnectionWrapper(inputConnection, true) {
        override fun sendKeyEvent(event: KeyEvent?): Boolean {
            if (event?.action == KeyEvent.ACTION_DOWN) {
                if (event?.keyCode == KeyEvent.KEYCODE_DEL) {
                    val targetString = getTextBeforeCursor(1, 0)
                    if (targetString == "\n") {
                        val spans = text?.getSpans(selectionStart, selectionEnd, MokaSpan::class.java)
                        val paragraphSpans = spans?.filter { it is ParagraphStyle }
                        if (paragraphSpans?.isNotEmpty() == true) {
                            Log.d(TAG, "paragraphSpans")
                            for (spanIndex in paragraphSpans.indices) {
                                text?.removeSpan(paragraphSpans[spanIndex])
                            }
                            return true
                        }
                    }
                } else if (event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                    val spans = text?.getSpans(selectionStart, selectionEnd, MokaSpan::class.java)
                    val paragraphSpans = spans?.filter { it is ParagraphStyle }
                    if (paragraphSpans?.isNotEmpty() == true) {
                        val otherSpan = paragraphSpans[0].copy()

                    }
                }
            }
            return super.sendKeyEvent(event)
        }

        override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
            Log.d(TAG, "commitText -> string : ${getTextBeforeCursor(1, 0)}")
            return super.commitText(text, newCursorPosition)
        }
    }

    /*override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection? {
        return MokaInputConnection(super.onCreateInputConnection(outAttrs))
    }*/

    inner class MokaTextWatcher : TextWatcher {
        var spanCollector: MokaSpanCollector? = null
        var start: Int = 0
        var before: Int = 0
        var after: Int = 0
        var isEntered = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (textWatcherEnabled) {
                if (s is SpannableStringBuilder && count > 0 && s.textWatcherDepth == 1) {
                    val beforeEnd = start+count
                    val subString = s.substring(start, beforeEnd)
                    if(subString.contains(MokaTextUtil.META_CHARACTER)){
                        Log.d(TAG, "remove paragraphSpan")
                        for(position in start until beforeEnd){
                            if(s[position] == MokaTextUtil.META_CHARACTER[0]){
                                val spans = s.getSpans(position, position, MokaSpan::class.java).filter { it is ParagraphStyle }
                                for(span in spans){
                                    s.removeSpan(span)
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            this.start = start
            this.before = before
            this.after = count
            isEntered = count == 1 && s?.substring(start, start + count)?.contains("\n") == true
            Log.d(TAG,"start : ${start}, before : $before, after : $after, str : ${s?.substring(start, start + after)}")
        }

        override fun afterTextChanged(s: Editable?) {
            if (textWatcherEnabled) {
                if (s is SpannableStringBuilder && s.textWatcherDepth == 1) {
                    if (isEntered) {
                        val spans = s.getSpans(start, start, MokaSpan::class.java).filter { (it is ParagraphStyle) }
                        for(span in spans){
                            val spanStart = s.getSpanStart(span)
                            val spanEnd = s.getSpanEnd(span)
                            val otherSpan = span.copy()
                            s.setSpan(span, spanStart, spanEnd-1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            if(span is MokaStrikeThroughParagraphSpan) continue
                            val afterEnd = start+after
                            s.insert(afterEnd, MokaTextUtil.META_CHARACTER)
                            s.setSpan(otherSpan, afterEnd, afterEnd+1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        }
                    }
                }
            }
        }
    }


    init {
        // hardwareAccelerator makes text layout overlapped vertically.
        //setLayerType(LAYER_TYPE_SOFTWARE, null)
        setLineSpacing(0f, 1.2f)
        addTextChangedListener(textWatcher)
    }

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val duration = (event.eventTime) - (event.downTime)
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (duration < CLICK_THRESHOLD) {
                        val clickableSpans = getClickableSpans(event.x, event.y)
                        if (clickableSpans?.isNotEmpty() == true) {
                            clickableSpans.forEach {
                                it.onClicked()
                            }
                            return true
                        }
                    }
                    return super.onTouchEvent(event)
                }
                MotionEvent.ACTION_DOWN -> {
                    val clickableSpans = getClickableSpans(event.x, event.y)
                    return if (clickableSpans?.isNotEmpty() == true)
                        true
                    else
                        super.onTouchEvent(event)
                }
                else -> {
                    return super.onTouchEvent(event)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getClickableSpans(x: Float, y: Float): List<MokaClickable>? {
        val positionFromTop: Int = scrollY + y.roundToInt() - totalPaddingTop
        val numberOfLine = layout.getLineForVertical(positionFromTop)
        val firstOfLine = layout.getLineStart(numberOfLine)
        val endOfLine = layout.getLineEnd(numberOfLine)

        val spans = text?.getSpans(firstOfLine, endOfLine, MokaClickable::class.java)
        return spans?.filter {
            val posX = x.roundToInt() - totalPaddingLeft
            val posY = y.roundToInt() - totalPaddingTop
            Log.d("RichCheckBoxSpan", "posX : $posX, posY : $posY")
            (posX in it.clickableLeft..it.clickableRight && posY in it.clickableTop..it.clickableBottom)
        }
    }

}