package com.luckyhan.studio.mokaeditor

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.util.Log
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import com.luckyhan.studio.mokaeditor.span.character.*
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaBulletSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaCheckBoxSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaQuoteSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaStrikeThroughParagraphSpan
import org.json.JSONArray
import org.json.JSONObject

class MokaSpanParser(private val context : Context) {

    fun getString(spannable: Spannable): String {
        val spans = spannable.getSpans(0, spannable.length, MokaSpan::class.java)
        val text = spannable.toString()
        val json = JSONObject()
        val jsonArray = JSONArray()
        json.put("text", text)
        spans.forEach {
            val start = spannable.getSpanStart(it)
            val end = spannable.getSpanEnd(it)
            val flag = spannable.getSpanFlags(it)
            val spanJson = JSONObject()
            spanJson.put("start", start)
            spanJson.put("end", end)
            spanJson.put("flag", flag)
            spanJson.put("name", it.getSpanTypeName())
            when (it) {
                is MokaCheckBoxSpan -> {
                    spanJson.put("checked", it.checked)
                }
                is MokaFontSizeSpan -> {
                    spanJson.put("proportion", it.proportion.toDouble())
                }
                is MokaForegroundColorSpan -> {
                    spanJson.put("color", it.color)
                }
                is MokaBackgroundColorSpan -> {
                    spanJson.put("color", it.color)
                }
            }
            jsonArray.put(spanJson)
        }
        json.put("spans", jsonArray)

        Log.d(this.javaClass.name, json.toString())

        return json.toString()
    }

    // TextView.setText dose not apply NoCopySpan.
    fun parseString(text: String): SpannableStringBuilder {
        val json = JSONObject(text)
        val rawText = json.getString("text")
        val spanArray = json.getJSONArray("spans")
        val spannable = SpannableStringBuilder(rawText)
        for (index in 0 until spanArray.length()) {
            val spanJson = spanArray.getJSONObject(index)
            val start = spanJson.getInt("start")
            val end = spanJson.getInt("end")
            val flag = spanJson.getInt("flag")
            val name = spanJson.getString("name")

            val span = when (name) {
                "checkbox" -> {
                    val checked = spanJson.getBoolean("checked")
                    MokaCheckBoxSpan(context , spannable, checked)
                }
                "quote" -> {
                    MokaQuoteSpan()
                }
                "strikethrough_paragraph" -> {
                    MokaStrikeThroughParagraphSpan()
                }
                "bullet" -> {
                    MokaBulletSpan()
                }
                "underline" -> {
                    MokaUnderlineSpan()
                }
                "bold" -> {
                    MokaBoldSpan()
                }
                "strikethrough" -> {
                    MokaStrikethroughSpan()
                }
                "fontsize" -> {
                    val proportion = spanJson.getDouble("proportion")
                    MokaFontSizeSpan(proportion.toFloat())
                }
                "foregroundcolor" -> {
                    val color = spanJson.getInt("color")
                    MokaForegroundColorSpan(color)
                }
                "backgroundcolor" -> {
                    val color = spanJson.getInt("color")
                    MokaBackgroundColorSpan(color)
                }
                else->{
                    throw Exception("Not supported span!")
                }
            }
            spannable.setSpan(span, start, end, flag)
        }
        return spannable
    }

    fun getRawText(source : String) : String{
        val json = JSONObject(source)
        val rawText = json.getString("text")
        return rawText
    }

    fun parseString(dest: Spannable, source : String){
        val json = JSONObject(source)
        val spanArray = json.getJSONArray("spans")
        for (index in 0 until spanArray.length()) {
            val spanJson = spanArray.getJSONObject(index)
            val start = spanJson.getInt("start")
            val end = spanJson.getInt("end")
            val flag = spanJson.getInt("flag")
            val name = spanJson.getString("name")

            val span = when (name) {
                "checkbox" -> {
                    val checked = spanJson.getBoolean("checked")
                    MokaCheckBoxSpan(context , dest, checked)
                }
                "quote" -> {
                    MokaQuoteSpan()
                }
                "strikethrough_paragraph" -> {
                    MokaStrikeThroughParagraphSpan()
                }
                "bullet" -> {
                    MokaBulletSpan()
                }
                "underline" -> {
                    MokaUnderlineSpan()
                }
                "bold" -> {
                    MokaBoldSpan()
                }
                "strikethrough" -> {
                    MokaStrikethroughSpan()
                }
                "fontsize" -> {
                    val proportion = spanJson.getDouble("proportion")
                    MokaFontSizeSpan(proportion.toFloat())
                }
                "foregroundcolor" -> {
                    val color = spanJson.getInt("color")
                    MokaForegroundColorSpan(color)
                }
                "backgroundcolor" -> {
                    val color = spanJson.getInt("color")
                    MokaBackgroundColorSpan(color)
                }
                else->{
                    throw Exception("Not supported span!")
                }
            }
            dest.setSpan(span, start, end, flag)
        }
    }
}