package com.luckyhan.studio.mokaeditor

import android.os.Parcel
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import com.luckyhan.studio.mokaeditor.span.MokaCharacterStyle
import com.luckyhan.studio.mokaeditor.span.MokaParagraphStyle
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import com.luckyhan.studio.mokaeditor.span.SelectionMarkupSpan
import com.luckyhan.studio.mokaeditor.span.character.MokaImageSpan
import com.luckyhan.studio.mokaeditor.util.MokaTextUtil
import kotlinx.coroutines.CoroutineScope

class MokaSpanTool(
    private val editText: MokaEditText,
    private val coroutineScope: CoroutineScope,
    private val parser: MokaSpanParser = DefaultMokaSpanParser()
) : MokaEditText.SelectionChangeListener,
    MokaEditText.TextChangeListener {

    fun restoreSavedInstanceState(source: Parcelable) {
        if (source is SavedState) {
            redoUndoStack = source.stack ?: ArrayList()
            stackCursor = source.stackCursor ?: -1
            updateToolStates()
        }
    }

    fun saveInstanceState(): Parcelable {
        return SavedState(stackCursor, redoUndoStack)
    }

    internal class SavedState(
        var stackCursor: Int? = null,
        var stack: ArrayList<Pair<String, Int>>? = null
    ) : Parcelable {


        constructor(parcel: Parcel) : this() {
            stackCursor = parcel.readInt()
            stack = parcel.readArrayList(null) as ArrayList<Pair<String, Int>>
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.writeInt(stackCursor ?: -1)
            dest?.writeList(stack)
        }


        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        val TAG = "MokaSpanTool"
    }

    private val spannable: SpannableStringBuilder
        get() {
            return editText.text as SpannableStringBuilder
        }

    var toolStateChangeListener: SpanToolStateChangeListener? = null
    var redoUndoDebounceInterval = 400L

    private var redoUndoStack: ArrayList<Pair<String, Int>> = ArrayList()
    private var stackCursor = -1
    private var redoUndoDebounce = MokaDebounce(redoUndoDebounceInterval, coroutineScope)
    private var redoUndoStackLocked = false
    private var redoUndoStackInitialized = false

    var redoable: Boolean = false
        private set
    var undoable: Boolean = false
        private set

    interface SpanToolStateChangeListener {
        fun onToolsStateChanged()
    }

    init {
        editText.selectionChangeListener = this
        editText.textChangeListener = this
    }


    private fun updateToolStates() {
        undoable = redoUndoStack.isNotEmpty() && stackCursor - 1 >= 0
        redoable = redoUndoStack.isNotEmpty() && redoUndoStack.size > stackCursor + 1
        toolStateChangeListener?.onToolsStateChanged()
    }

    fun setImageSpan(span: MokaImageSpan) {
        val selectionEnd = editText.selectionEnd
        editText.setSelection(selectionEnd)
        spannable.insert(selectionEnd, MokaTextUtil.IMAGE_PLACEHOLDER_CHARACTER)
        spannable.setSpan(span, selectionEnd + 1, selectionEnd + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    //toggle spans
    fun toggleParagraphStyleSpan(span: MokaSpan) {
        if (span !is MokaParagraphStyle) return
        val selectionMarkup = SelectionMarkupSpan()
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        spannable.setSpan(selectionMarkup, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        val paragraphSpans = spannable.getSpans(selectionStart, selectionEnd, MokaParagraphStyle::class.java)
        val thatSpans = spannable.getSpans(selectionStart, selectionEnd, span.javaClass)
        val requestRemove = paragraphSpans.isNotEmpty()
        val requestSet = thatSpans.isEmpty()
        if (requestRemove) removeParagraphSpan(MokaParagraphStyle::class.java, selectionStart, selectionEnd)
        val spanStart = spannable.getSpanStart(selectionMarkup)
        val spanEnd = spannable.getSpanEnd(selectionMarkup)
        if (requestSet) addParagraphSpan(span, spanStart, spanEnd)
        spannable.removeSpan(selectionMarkup)
    }

    fun toggleCharacterStyleSpan(span: MokaSpan) {
        if (span !is MokaCharacterStyle) return
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val isExistSpan = isThereSpan(span.javaClass)
        if (isExistSpan) {
            removeCharacterSpan(span.javaClass, selectionStart, selectionEnd)
        } else {
            addCharacterSpan(span)
        }
    }

    fun replaceCharacterStyleSpan(span: MokaSpan) {
        if (span !is MokaCharacterStyle) return
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        removeCharacterSpan(span.javaClass, selectionStart, selectionEnd)
        addCharacterSpan(span)
    }

    fun <T> isThereSpan(spanType: Class<T>): Boolean {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        return spans.isNotEmpty()
    }

    fun <T> getSpans(spanType: Class<T>): List<T> {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        return spans.toList()
    }

    private fun addCharacterSpan(span: MokaSpan) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        spannable.setSpan(span, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        updateToolStates()
    }

    fun <T> removeCharacterSpan(classType: Class<T>){
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        removeCharacterSpan(classType, selectionStart, selectionEnd)
    }

    private fun <T> removeCharacterSpan(classType: Class<T>, selectionStart: Int, selectionEnd: Int) {
        val spans = spannable.getSpans(selectionStart, selectionEnd, classType)

        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            if ((spanStart in selectionStart..selectionEnd) && (spanEnd in selectionStart..selectionEnd)) {
                //just remove span
            } else if ((spanStart in selectionStart..selectionEnd) && (spanEnd > selectionEnd)) {
                spannable.setSpan(span, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            } else if ((spanEnd in selectionStart..selectionEnd) && (spanStart < selectionStart)) {
                spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            } else if ((spanStart < selectionStart && selectionStart < spanEnd - 1) && (selectionEnd in (spanStart + 1) until spanEnd)) {
                if (span is MokaSpan) {
                    val otherSpan = span.copy()
                    spannable.setSpan(span, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    spannable.setSpan(otherSpan, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            } else {
                throw UnsupportedOperationException("this span range is out of use cases!")
            }
        }
        updateToolStates()
    }

    private fun addParagraphSpan(span: MokaSpan, selectionStart: Int, selectionEnd: Int) {
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
            start = end + 1

        }
        updateToolStates()
    }

    private fun <T> removeParagraphSpan(classType: Class<T>, selectionStart: Int, selectionEnd: Int) {
        val spans = spannable.getSpans(selectionStart, selectionEnd, classType)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            if (spanStart + 1 <= spannable.length && spannable.substring(spanStart, spanStart + 1) == MokaTextUtil.META_CHARACTER) {
                spannable.replace(spanStart, spanStart + 1, "")
            }
        }
        updateToolStates()
    }

    override fun onSelectionChanged() {
        updateToolStates()
    }

    override fun onTextChanged() {
        if (!redoUndoStackInitialized) {
            redoUndoStackInitialized = true
            return
        }
        if (!redoUndoStackLocked) {
            redoUndoDebounce.request {
                dumpStackItems()
                val currentContent = parser.getString(spannable)
                redoUndoStack.add(Pair(currentContent, editText.selectionStart))
                stackCursor = redoUndoStack.size - 1
            }
        }
    }

    private fun dumpStackItems() {
        while (stackCursor < redoUndoStack.size - 1) {
            redoUndoStack.removeLast()
        }
    }

    fun redo() {
        if (redoUndoStack.isNotEmpty() && stackCursor + 1 < redoUndoStack.size) {
            stackCursor++
            val text = redoUndoStack[stackCursor]
            restoreSpansFromJson(text.first, text.second)
        }
    }

    fun undo() {
        if (redoUndoStack.isNotEmpty() && stackCursor - 1 >= 0) {
            stackCursor--
            val text = redoUndoStack[stackCursor]
            restoreSpansFromJson(text.first, text.second)
        }
    }

    private fun restoreSpansFromJson(jsonString: String, selection : Int) {
        val rawText = parser.getRawText(jsonString)
        editText.textWatcherEnabled = false
        redoUndoStackLocked = true
        editText.setText(rawText)
        redoUndoStackLocked = false
        editText.textWatcherEnabled = true
        parser.parseString(editText, jsonString)
        if(editText.length() > selection) editText.setSelection(selection)
        //editText.setSelection(spannable.length)
    }


}