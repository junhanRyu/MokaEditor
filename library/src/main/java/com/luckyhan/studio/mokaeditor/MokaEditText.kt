package com.luckyhan.studio.mokaeditor

import android.R
import android.R.attr
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.*
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ViewTreeObserver
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.AppCompatEditText
import com.luckyhan.studio.mokaeditor.span.MokaClickable
import com.luckyhan.studio.mokaeditor.span.MokaParagraphStyle
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaStrikeThroughParagraphSpan
import com.luckyhan.studio.mokaeditor.util.MokaTextUtil
import kotlin.math.roundToInt
import android.R.attr.mode

import android.widget.Toast


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import kotlin.math.max
import kotlin.math.min


class MokaEditText : AppCompatEditText {

    companion object {
        val TAG = "MokaEditText"
    }

    private val textWatcher = MokaTextWatcher()
    private val CLICK_THRESHOLD = 100

    var selectionChangeListener: SelectionChangeListener? = null
    var textChangeListener: TextChangeListener? = null
    var spanClickListener: MokaSpanClickListener? = null
    var spanParser: MokaSpanParser = DefaultMokaSpanParser()
    var isEnabledClickable = true
    var textWatcherEnabled = true

    private val clipBoard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    interface SelectionChangeListener {
        fun onSelectionChanged()
    }

    interface TextChangeListener {
        fun onTextChanged()
    }

    interface MokaSpanClickListener {
        fun onSpanClicked(span: MokaClickable)
    }

    init {
        addTextChangedListener(textWatcher)
        customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
                return true
            }

            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: android.view.ActionMode?, item: MenuItem?): Boolean {
                item?.let{
                    when (item.itemId) {
                        R.id.copy -> {
                            val contents = text ?: ""
                            var min = 0
                            var max: Int = contents.length
                            if (isFocused) {
                                val selStart: Int = selectionStart
                                val selEnd: Int = selectionEnd
                                min = max(0, min(selStart, selEnd))
                                max = max(0, max(selStart, selEnd))
                            }
                            val subString = contents.substring(min, max)
                            subString.replace(MokaTextUtil.META_CHARACTER,"")
                            // Perform your definition lookup with the selected text
                            val text = subString.toString()
                            val clip = ClipData.newPlainText("text", text)
                            clipBoard.setPrimaryClip(clip)
                            // Finish and close the ActionMode
                            mode?.finish()
                            Log.d(TAG, "copied")
                            return true
                        }
                        R.id.cut -> {
                            val contents = text ?: ""
                            var min = 0
                            var max: Int = contents.length
                            if (isFocused) {
                                val selStart: Int = selectionStart
                                val selEnd: Int = selectionEnd
                                min = max(0, min(selStart, selEnd))
                                max = max(0, max(selStart, selEnd))
                            }
                            val subString = contents.substring(min, max)
                            subString.replace(MokaTextUtil.META_CHARACTER,"")
                            // Perform your definition lookup with the selected text
                            val text = subString.toString()
                            val clip = ClipData.newPlainText("text", text)
                            clipBoard.setPrimaryClip(clip)
                            // Finish and close the ActionMode
                            val spannableStringBuilder = SpannableStringBuilder.valueOf(contents)
                            spannableStringBuilder.replace(min, max, "")
                            mode?.finish()
                            Log.d(TAG, "cut")
                            return true
                        }
                        R.id.paste ->
                            return false
                        else -> {
                        }
                    }
                }
                return false
            }

            override fun onDestroyActionMode(mode: android.view.ActionMode?) {

            }
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        val updateSpan = UpdateSpan()
        this.text?.setSpan(updateSpan, 0, this.text?.length ?: 0, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        val source = text.toString()

        // adjust selection cursor because we have to hide special characters to users.
        if (selStart == selEnd && selStart < source.length &&
            (source.isNotEmpty()) &&
            source.substring(selStart, selStart + 1) == MokaTextUtil.META_CHARACTER
        ) {
            setSelection(selStart + 1)
        } else if (selStart == selEnd && selStart < source.length &&
            (source.isNotEmpty()) &&
            source.substring(selStart, selStart + 1)[0] == MokaTextUtil.IMAGE_PLACEHOLDER_CHARACTER[1]
        ) {
            setSelection(selStart - 1)
        } else {
            super.onSelectionChanged(selStart, selEnd)
            // setSelection will be called recursively. so, this listener will called always even if selection is the use cases of special characters.
            selectionChangeListener?.onSelectionChanged()
        }
    }

    inner class MokaTextWatcher : TextWatcher {
        var start: Int = 0
        var before: Int = 0
        var after: Int = 0
        var isEntered = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // we have to check textWatcherDepth. because this listener can be called recursively.
            if (s is SpannableStringBuilder && count > 0 && s.textWatcherDepth == 1 && textWatcherEnabled) {
                val beforeEnd = start + count
                val subString = s.substring(start, beforeEnd)
                if (subString.contains(MokaTextUtil.META_CHARACTER)) {
                    for (position in start until beforeEnd) {
                        if (s[position] == MokaTextUtil.META_CHARACTER[0]) {
                            val spans = s.getSpans(position, position, MokaParagraphStyle::class.java)
                            for (span in spans) {
                                s.removeSpan(span)
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
            isEntered = count == 1 && (s?.substring(start, start + count)?.contains("\n") == true)
        }

        override fun afterTextChanged(s: Editable?) {
            // we have to check textWatcherDepth. because this listener can be called recursively.
            if (s is SpannableStringBuilder && s.textWatcherDepth == 1 && textWatcherEnabled) {
                if (isEntered) {
                    val spans = s.getSpans(start, start, MokaParagraphStyle::class.java)
                    for (span in spans) {
                        val spanStart = s.getSpanStart(span)
                        val otherSpan = span.copy()
                        val afterEnd = start + after
                        s.setSpan(span, spanStart, start, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        if (span is MokaStrikeThroughParagraphSpan) continue
                        s.insert(afterEnd, MokaTextUtil.META_CHARACTER)
                        val lineEnd = MokaTextUtil.getEndOfLine(s.toString(), afterEnd)
                        s.setSpan(otherSpan, afterEnd, lineEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
                }
                textChangeListener?.onTextChanged()
            }
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isEnabledClickable && event != null) {
            val duration = (event.eventTime) - (event.downTime)
            val clickableSpans = getClickableSpans(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (duration < CLICK_THRESHOLD) {
                        if (clickableSpans?.isNotEmpty() == true) {
                            clickableSpans.forEach {
                                it.onClicked()
                                spanClickListener?.onSpanClicked(it)
                            }
                            return true
                        }
                    }
                    return super.onTouchEvent(event)
                }
                MotionEvent.ACTION_DOWN -> {
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
            (posX in it.clickableLeft..it.clickableRight && posY in it.clickableTop..it.clickableBottom)
        }
    }

    // very tricky. spans have to be restored after layouting edittext view. so, this callback is used.
    // if I can find any better way to resolve this problem, I have to refactor this implementation.
    interface GlobalLayoutListenerCallback {
        fun finished()
    }

    inner class GlobalLayoutListener(
        var contents: String? = null,
        var callback: GlobalLayoutListenerCallback? = null
    ) : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            contents?.let {
                val source = it
                spanParser.parseString(this@MokaEditText, source)
            }
            callback?.finished()
        }
    }

    private val globalLayoutListerCallback = object : GlobalLayoutListenerCallback {
        override fun finished() {
            viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    private val globalLayoutListener = GlobalLayoutListener()

    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        return SavedState(parcelable).apply {
            spannedContents = spanParser.getString(text as Spannable)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                state.spannedContents?.let {
                    globalLayoutListener.contents = it
                    globalLayoutListener.callback = globalLayoutListerCallback
                    viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
                }
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    internal class SavedState : BaseSavedState {

        var spannedContents: String? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            spannedContents = source.readString()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(spannedContents)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}