package com.luckyhan.studio.richeditor

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle
import android.text.style.ParagraphStyle
import android.util.Log
import android.widget.EditText
import kotlin.IllegalStateException

class Selection(private val editText: EditText, private var selectionStart: Int, private var selectionEnd: Int) {
/*
    private val paragraphBeginningChar = "\u200B"

    private var previousText: String? = null
    private val spannable = editText.text
    private val paragraphs: ArrayList<Paragraph> = ArrayList()
    private val characterSpans: ArrayList<SpanModel> = ArrayList()
    private var removedParagraphs = 0

    fun onBeforeTextChanged() {
        previousText = spannable.toString()
        storeCharacterSpans()
        storeParagraphs()
        removedParagraphs = removeAllParagraphSpans()
    }

    fun onAfterTextChanged(start: Int, lengthBefore: Int, lengthAfter: Int) {
        removeTextWatcher()
        restoreCharacterSpans(start - removedParagraphs, lengthBefore, lengthAfter)
        restoreParagraphSpans(start - removedParagraphs, lengthBefore, lengthAfter)
        setTextWatcher()
    }

    fun <T> isThereSpan(spanType: Class<T>): Boolean {
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        return spans.isNotEmpty()
    }

    private fun storeCharacterSpans() {
        val spans = spannable.getSpans(0, spannable.length, CharacterStyle::class.java)
        for (span in spans) {
            if (span is RichSpannable) {
                val spanStart = spannable.getSpanStart(span)
                val spanEnd = spannable.getSpanEnd(span)
                val spanFlag = spannable.getSpanFlags(span)
                characterSpans.add(SpanModel(span, spanFlag, spanStart, spanEnd))
            }
        }
    }

    private fun restoreCharacterSpans(start: Int, before: Int, after: Int) {
        if (previousText == null) {
            throw IllegalStateException("Spans are not stored before!")
        } else {
            val afterEnd = start + after
            val beforeEnd = start + before
            val offset = after - before
            Log.d("selection", "start : $start, before : $before, after : $after")
            Log.d("selection", "afterEnd : $afterEnd, beforeEnd : $beforeEnd, offset : $offset")
            for (spanModel in characterSpans) {
                val span = spanModel.span
                val spanStart = spanModel.start
                val spanEnd = spanModel.end
                val flag = spanModel.flag
                if (spanEnd < start) {
                    spannable.setSpan(span, spanStart, spanEnd, flag)
                } else if ((beforeEnd in spanStart until spanEnd && start <= spanStart)) {
                    if (flag == Spannable.SPAN_INCLUSIVE_INCLUSIVE || flag == Spannable.SPAN_INCLUSIVE_EXCLUSIVE) {
                        //stretch spans
                        spannable.setSpan(span, start, spanEnd + offset, flag)
                    } else {
                        spannable.setSpan(span, afterEnd, spanEnd + offset, flag)
                    }
                } else if (start in spanStart..spanEnd && beforeEnd >= spanEnd) {
                    if (flag == Spannable.SPAN_INCLUSIVE_INCLUSIVE || flag == Spannable.SPAN_EXCLUSIVE_INCLUSIVE) {
                        //stretch spans
                        spannable.setSpan(span, spanStart, afterEnd, flag)
                    } else {
                        spannable.setSpan(span, spanStart, start, flag)
                    }
                } else if (spanStart < start && beforeEnd <= spanEnd) {
                    //stretch spans
                    spannable.setSpan(span, spanStart, spanEnd + offset, flag)
                } else if (spanStart >= start && spanEnd <= beforeEnd) {
                    //don't restore spans
                } else if (beforeEnd < spanStart) {
                    spannable.setSpan(span, spanStart, spanEnd, flag)
                } else {
                    Log.e("selection", "spanStart : $spanStart, spanEnd : $spanEnd, textStart : $start, textEnd : $beforeEnd")
                    throw IllegalStateException("out of ranges!")
                }
            }
        }
    }

    private fun removeAllCharacterSpans() {
        for (span in characterSpans) {
            spannable.removeSpan(span)
        }
    }

    private fun removeAllParagraphSpans(): Int {
        var removed = 0
        removeTextWatcher()
        for (paragraph in paragraphs) {
            if (paragraph.isSpans()) {
                if (spannable is SpannableStringBuilder) {
                    paragraph.removeSpans(spannable)
                    if(paragraph.start != 0){
                        paragraph.start -= removed
                        paragraph.end -= removed
                    }
                    removeParagraphBeginningChar(spannable, paragraph.start)
                    paragraph.end--
                    removed++
                }
            }
        }
        setTextWatcher()
        return removed
    }

    fun setCharacterSpan(span: CharacterStyle) {
        spannable.setSpan(span, selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    fun <T> removeCharacterSpan(type: Class<T>) {
        val spans = spannable.getSpans(selectionStart, selectionEnd, type)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            if (selectionEnd in spanStart until spanEnd && selectionStart <= spanStart) {
                spannable.setSpan(span, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            } else if (selectionStart in spanStart..spanEnd && selectionEnd >= spanEnd) {
                spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            } else if (spanStart < selectionStart && selectionEnd <= spanEnd) {
                if (span is RichSpannable) {
                    val oppositeSpan = span.copy()
                    spannable.setSpan(span, spanStart, selectionStart, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(oppositeSpan, selectionEnd, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                } else {
                    throw IllegalStateException("this span is not supported!")
                }
            } else if (spanStart >= selectionStart && spanEnd <= selectionEnd) {
                //already done
            } else {
                throw IllegalStateException("out of ranges!")
            }
        }
    }

    private fun storeParagraphs() {
        val lineStart = getStartOfLine(spannable.toString(), 0)
        val lineEnd = getEndOfLine(spannable.toString(), spannable.toString().length)
        val subString = spannable.toString().substring(lineStart, lineEnd)
        if (subString.isNotEmpty()) {
            val lines = subString.split('\n')
            var start = lineStart
            var end = lineStart
            for (line in lines) {
                end += line.length
                val paragraph = Paragraph(start, end)
                paragraph.storeSpans(spannable)
                paragraphs.add(paragraph)
                start += end + 1
                end = start
            }
        }
    }

    private fun restoreParagraphSpans(start: Int, before: Int, after: Int) {
        if (previousText == null) {
            throw IllegalStateException("Spans are not stored before!")
        } else {
            var textStart = start
            if (spannable is SpannableStringBuilder) {
                for (paragraph in paragraphs) {
                    if (paragraph.isSpans()) {
                        val afterEnd = textStart + after
                        val beforeEnd = textStart + before
                        val offset = after - before
                        var paragraphStart = paragraph.start
                        var paragraphEnd = paragraph.end
                        if (paragraphEnd < textStart) {
                            addParagraphBeginningChar(spannable, paragraphStart)
                            paragraphEnd++
                            textStart++
                            paragraph.restoreSpans(spannable, paragraphStart, paragraphEnd)
                        } else if ((beforeEnd in paragraphStart until paragraphEnd && textStart <= paragraphStart)) {
                            if (previousText?.substring(textStart, beforeEnd)?.contains(paragraphBeginningChar) == true) {
                                Log.d("selection", "paragraph deleted!")
                            } else {
                                addParagraphBeginningChar(spannable, paragraphStart)
                                paragraphEnd++
                                textStart++
                                paragraph.restoreSpans(spannable, paragraphStart, paragraphEnd + offset)
                            }
                        } else if (textStart in paragraphStart..paragraphEnd && beforeEnd >= paragraphEnd) {
                            if (offset == 1 && spannable.substring(textStart, afterEnd) == "\n") {
                                Log.d("selection", "new line!")
                                addParagraphBeginningChar(spannable, paragraphStart)
                                textStart++
                                paragraph.restoreSpans(spannable, paragraphStart, textStart)
                                addParagraphBeginningChar(spannable, textStart + 1)
                                textStart++
                                paragraph.copySpans(spannable, textStart, textStart + 1)
                                editText.setSelection(textStart+1)
                            } else {
                                val lineEnd = getEndOfLine(spannable.toString(), paragraphStart)
                                addParagraphBeginningChar(spannable, paragraphStart)
                                textStart++
                                paragraph.restoreSpans(spannable, paragraphStart, lineEnd + 1)
                            }
                        } else if (paragraphStart < textStart && beforeEnd <= paragraphEnd) {
                            val lineEnd = getEndOfLine(spannable.toString(), paragraphStart)
                            addParagraphBeginningChar(spannable, paragraphStart)
                            textStart++
                            paragraph.restoreSpans(spannable, paragraphStart, lineEnd + 1)
                        } else if (paragraphStart >= textStart && paragraphEnd <= beforeEnd) {
                            //don't restore
                        } else if (beforeEnd < paragraphStart) {
                            addParagraphBeginningChar(spannable, paragraphStart)
                            paragraph.restoreSpans(spannable, paragraphStart + offset, paragraphEnd + offset + 1)
                        } else {
                            Log.e(
                                "selection",
                                "paragraphStart : $paragraphStart, paragraphEnd : $paragraphEnd, textStart : $textStart, textEnd : $beforeEnd"
                            )
                            throw IllegalStateException("out of ranges!")
                        }
                    }
                }
            }
        }
    }

    fun setParagraphSpan(span: RichSpannable) {
        val lineStart = getStartOfLine(spannable.toString(), selectionStart)
        val lineEnd = getEndOfLine(spannable.toString(), selectionEnd)
        val subString = spannable.toString().substring(lineStart, lineEnd)
        val lines = subString.split('\n')
        var start = lineStart
        var end = lineStart
        removeTextWatcher()
        for (line in lines) {
            val otherSpan = span.copy()
            end += line.length
            if (spannable is SpannableStringBuilder) {
                addParagraphBeginningChar(spannable, start)
                end++
                spannable.setSpan(otherSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                start++
            }
            start += line.length + 1
            end = start
        }
        setTextWatcher()
    }

    private fun removeParagraphBeginningChar(spannableStringBuilder: SpannableStringBuilder, paragraphStart: Int) {
        spannableStringBuilder.delete(paragraphStart, paragraphStart + 1)
    }

    private fun addParagraphBeginningChar(spannableStringBuilder: SpannableStringBuilder, paragraphStart: Int) {
        spannableStringBuilder.insert(paragraphStart, paragraphBeginningChar)
    }

    fun <T> removeParagraphSpan(spanType: Class<T>) {
        val spans = spannable.getSpans(selectionStart, selectionEnd, spanType)
        //TextWatcher has to be removed from here
        removeTextWatcher()
        for (span in spans) {
            if (spannable is SpannableStringBuilder) {
                val start = spannable.getSpanStart(span)
                spannable.removeSpan(span)
                removeParagraphBeginningChar(spannable, start)
            }
        }
        setTextWatcher()
    }

    private fun removeTextWatcher() {
        if (editText is RichEditText) {
            editText.removeTextChangedListener(editText.textWatcher)
        }
    }

    private fun setTextWatcher() {
        if (editText is RichEditText) {
            editText.addTextChangedListener(editText.textWatcher)
        }
    }

    private fun getStartOfLine(text: String, offset: Int): Int {
        if (offset < 0) throw IndexOutOfBoundsException("offset is negative!")
        if (text.isEmpty()) return 0
        var lineStart = offset
        while (lineStart > 0) {
            if (text[lineStart - 1] == '\n') break
            lineStart--
        }
        return lineStart
    }

    private fun getEndOfLine(text: String, offset: Int): Int {
        if (offset < 0) throw IndexOutOfBoundsException("offset is negative!")
        if (offset == text.length) return text.length
        if (text.isEmpty()) return 0
        var lineEnd = offset
        while (lineEnd < text.length) {
            if (text[lineEnd] == '\n') break
            lineEnd++
        }
        return lineEnd
    }*/
}