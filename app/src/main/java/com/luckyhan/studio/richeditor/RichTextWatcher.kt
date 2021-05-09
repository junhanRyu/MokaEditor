package com.luckyhan.studio.richeditor

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ParagraphStyle
import android.util.Log
import android.widget.EditText

class RichTextWatcher(private val editText : EditText) : TextWatcher {

    private var selectionStart : Int = 0
    private var selectionEnd : Int = 0
    private var previousString : String = ""
    private val zeroWidthChar = "\u200B"
    private var firstParagraphSpans : Array<ParagraphStyle>? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //Log.d("span", "text -> start : ${start}, after : ${after}, count : ${count}")
        s?.let {
            previousString = editText.text.toString()
            keepSelectionPosition()
            if(!isCursor()) removeSpansInRange(selectionStart, selectionEnd)
            firstParagraphSpans = getParagraphSpans(getStartOfLine(previousString, selectionStart), getEndOfLine(previousString, selectionStart))
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        //Log.d("span", "text -> size : ${s?.length} ch : ${s?.get(start)?.toInt()}, start : ${start}, before : ${before}, count : ${count}")
        //Log.d("span", "text -> start : ${start}, before : ${before}, count : ${count}")
        s?.let {

        }
    }

    override fun afterTextChanged(s: Editable?) {

    }

    private fun getStartOfLine(text : String, offset : Int) : Int{
        if(offset < 0) return -1
        if(text.isEmpty()) return 0
        var start = offset
        while(start > 0){
            if(text[start - 1] == '\n') break
            start--
        }
        return start
    }

    private fun getEndOfLine(text : String, offset : Int) : Int{
        if(offset < 0) return -1
        if(text.isEmpty()) return 0
        var end = offset
        while(end < text.length){
            if(text[end] == '\n') break
            end++
        }
        return end
    }

    private fun addZeroWidthCharacter(offset : Int){
        editText.text.insert(offset ,zeroWidthChar)
    }

    private fun isCursor() : Boolean{
        return (editText.selectionStart == editText.selectionEnd)
    }

    private fun keepSelectionPosition(){
        selectionStart = editText.selectionStart
        selectionEnd = editText.selectionEnd
    }

    private fun getParagraphSpans(start : Int, end : Int) : Array<ParagraphStyle>{
        return editText.text.getSpans(start, end, ParagraphStyle::class.java)
    }

    private fun removeSpansInRange(start : Int, end : Int){
        val spans = editText.text.getSpans(start, end, Any::class.java)
        for(span in spans){
            val spanStart = editText.text.getSpanStart(span)
            if(spanStart >= start) editText.text.removeSpan(span)
        }
    }
}