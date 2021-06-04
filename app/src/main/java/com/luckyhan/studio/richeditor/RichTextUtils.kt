package com.luckyhan.studio.richeditor

object RichTextUtils {

    fun getStartOfLine(text: String, offset: Int): Int {
        if (offset < 0 || offset > text.length)
            throw Exception("invalid offset! length = ${text.length}, offset = $offset")
        if (text == "") return 0
        var lineStart = offset
        while (lineStart > 0) {
            if (text[lineStart - 1] == '\n') break
            lineStart--
        }
        return lineStart
    }

    fun getEndOfLine(text: String, offset: Int): Int {
        if (offset < 0 || offset > text.length)
            throw Exception("invalid offset! length = ${text.length}, offset = $offset")
        if (offset == text.length) return text.length
        if (text == "") return 0
        var lineEnd = offset
        while (lineEnd < text.length) {
            if (text[lineEnd] == '\n') break
            lineEnd++
        }
        return lineEnd
    }
}