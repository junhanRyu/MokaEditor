package com.luckyhan.studio.mokaeditor

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
import com.luckyhan.studio.mokaeditor.span.MokaClickable
import kotlin.math.roundToInt

class MokaEditText : AppCompatEditText {
    var isTouched = false
    var selectionChangeListenr : RichSelectionChangeListener? = null

    interface RichSelectionChangeListener{
        fun onSelectionChanged()
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        selectionChangeListenr?.onSelectionChanged()
    }

    private val textWatcher = object : TextWatcher {
        var spanCollector: MokaSpanCollector? = null
        var start: Int = 0
        var before: Int = 0
        var after: Int = 0

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (s is Spannable) {
                spanCollector = MokaSpanCollector()
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
                    val composer = MokaSpanComposer()
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



    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                if (isTouched) {
                    isTouched = false
                    val clickableSpans = getClickableSpans(event.x, event.y)
                    if (clickableSpans?.isNotEmpty() == true){
                        clickableSpans.forEach{
                            it.onClicked()
                        }
                        return true
                    }
                }
                return super.onTouchEvent(event)
            }
            MotionEvent.ACTION_DOWN -> {
                isTouched = true
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
            (posX in it.clickableLeft .. it.clickableRight && posY in it.clickableTop .. it.clickableBottom)
        }
    }

}