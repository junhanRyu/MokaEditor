package com.luckyhan.studio.mokaeditor

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatEditText
import com.luckyhan.studio.mokaeditor.span.MokaClickable
import com.luckyhan.studio.mokaeditor.span.MokaParagraphStyle
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
    var spanParser : MokaSpanParser = DefaultMokaSpanParser()

    interface SelectionChangeListener {
        fun onSelectionChanged()
    }

    interface TextChangeListener {
        fun onTextChanged()
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        val source = text.toString()

        if(selStart == selEnd && selStart < source.length &&
            (source.isNotEmpty()) &&
            source.substring(selStart, selStart+1) == MokaTextUtil.META_CHARACTER){
            setSelection(selStart+1)
        }
        else if(selStart == selEnd && selStart < source.length &&
            (source.isNotEmpty()) &&
            source.substring(selStart, selStart+1)[0] == MokaTextUtil.IMAGE_PLACEHOLDER_CHARACTER[1]){
            setSelection(selStart-1)
        }
        else{
            super.onSelectionChanged(selStart, selEnd)
            selectionChangeListenr?.onSelectionChanged()
        }
    }

    inner class MokaTextWatcher : TextWatcher {
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
                        for(position in start until beforeEnd){
                            if(s[position] == MokaTextUtil.META_CHARACTER[0]){
                                val spans = s.getSpans(position, position, MokaParagraphStyle::class.java)
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
                        Log.d(TAG,"Entered!")
                        val spans = s.getSpans(start, start, MokaParagraphStyle::class.java)
                        for(span in spans){
                            val spanStart = s.getSpanStart(span)
                            val otherSpan = span.copy()
                            val afterEnd = start+after
                            s.setSpan(span, spanStart, start, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            if(span is MokaStrikeThroughParagraphSpan) continue
                            s.insert(afterEnd, MokaTextUtil.META_CHARACTER)
                            val lineEnd = MokaTextUtil.getEndOfLine(s.toString(), afterEnd)
                            s.setSpan(otherSpan, afterEnd, lineEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        }
                    }
                }
            }
            textChangeListener?.onTextChanged()
        }
    }


    init {
        // hardwareAccelerator makes text layout overlapped vertically. user's application should disable hardwareAcceleration in Manifest like below
        // <application android:hardwareAccelerated="false" ...>
        //setLayerType(LAYER_TYPE_SOFTWARE, null)
        //setLineSpacing(0f, 1.2f)
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
            (posX in it.clickableLeft..it.clickableRight && posY in it.clickableTop..it.clickableBottom)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        return SavedState(parcelable).apply{
            spannedContents = spanParser.getString(text as Spannable)
        }
    }

    // very tricky. spans have to be restored after layouting edittext view. so, this callback is used.
    // if I can find any better way to resolve this problem, I have to refactor this implementation.
    interface GlobalLayoutListenerCallback{
        fun finished()
    }

    inner class GlobalLayoutListenr(
        var contents : String? = null,
        var callback : GlobalLayoutListenerCallback? = null)
        : ViewTreeObserver.OnGlobalLayoutListener{
        override fun onGlobalLayout() {
            contents?.let{
                val source = it
                spanParser.parseString(this@MokaEditText, source)
            }
            callback?.finished()
        }
    }

    private val globalLayoutListerCallback = object : GlobalLayoutListenerCallback{
        override fun finished() {
            viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    val globalLayoutListener = GlobalLayoutListenr()

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

        var spannedContents : String? = null

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