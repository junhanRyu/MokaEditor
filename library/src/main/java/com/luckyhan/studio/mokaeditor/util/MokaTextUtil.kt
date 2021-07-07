package com.luckyhan.studio.mokaeditor.util

import java.lang.UnsupportedOperationException

object MokaTextUtil {

    val META_CHARACTER = "\uFEFF"

    fun getStartOfLine(text: String, offset: Int): Int {
        if (offset < 0 || offset > text.length)
            throw UnsupportedOperationException("offset is out of range. length = ${text.length}, offset = $offset")
        var lineStart = offset
        while (lineStart > 0) {
            if (text[lineStart - 1] == '\n') break
            lineStart--
        }
        return lineStart
    }

    // either the position of \n or the end of text
    fun getEndOfLine(text: String, offset: Int): Int {
        if (offset < 0 || offset > text.length)
            throw UnsupportedOperationException("offset is out of range. length = ${text.length}, offset = $offset")
        if (text == "") return 0
        var lineEnd = offset
        while (lineEnd < text.length) {
            if (text[lineEnd] == '\n') break
            lineEnd++
        }
        return lineEnd
    }

    fun getPreviousLine(text : String, currentLineStart : Int) : String{
        val offset = currentLineStart - 1
        if (offset < 0 || offset > text.length)
            throw UnsupportedOperationException("offset is out of range. length = ${text.length}, offset = $offset")
        if (text[offset] != '\n')
            throw UnsupportedOperationException("this line is first line")
        val previousLineStart  = getStartOfLine(text, offset)
        val previousLineEnd = getEndOfLine(text, offset)

        return if(previousLineStart == previousLineEnd)
            ""
        else
            text.substring(previousLineStart, previousLineEnd)
    }
}