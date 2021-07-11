package com.luckyhan.studio.mokaeditor

import android.text.Spannable
import android.util.Log
import android.view.View
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import com.luckyhan.studio.mokaeditor.span.character.*
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaBulletSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaCheckBoxSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaQuoteSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaStrikeThroughParagraphSpan
import org.json.JSONArray
import org.json.JSONObject
import java.lang.UnsupportedOperationException

class DefaultMokaSpanParser : MokaSpanParser {
    override fun createSpan(sourceJson: JSONObject, dest: MokaEditText): MokaSpan {
        val name = sourceJson.getString("name")
        Log.d("DefaultMokaSpanParser", name)
        return when (name) {
            MokaCheckBoxSpan::class.java.name -> {
                val checked = sourceJson.getBoolean("checked")
                MokaCheckBoxSpan(dest, checked)
            }
            MokaQuoteSpan::class.java.name -> {
                MokaQuoteSpan()
            }
            MokaStrikeThroughParagraphSpan::class.java.name -> {
                MokaStrikeThroughParagraphSpan()
            }
            MokaBulletSpan::class.java.name -> {
                MokaBulletSpan()
            }
            MokaUnderlineSpan::class.java.name -> {
                MokaUnderlineSpan()
            }
            MokaBoldSpan::class.java.name -> {
                MokaBoldSpan()
            }
            MokaStrikethroughSpan::class.java.name -> {
                MokaStrikethroughSpan()
            }
            MokaFontSizeSpan::class.java.name -> {
                val proportion = sourceJson.getDouble("proportion")
                MokaFontSizeSpan(proportion.toFloat())
            }
            MokaForegroundColorSpan::class.java.name -> {
                val color = sourceJson.getInt("color")
                MokaForegroundColorSpan(color)
            }
            MokaBackgroundColorSpan::class.java.name -> {
                val color = sourceJson.getInt("color")
                MokaBackgroundColorSpan(color)
            }
            MokaImageSpan::class.java.name -> {
                val imageName = sourceJson.getString("image")
                MokaImageSpan(dest, imageName)
            }
            else -> {
                throw UnsupportedOperationException("Not supported span!")
            }
        }
    }

    override fun getString(spannable: Spannable): String {
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
            spanJson.put("name", it.javaClass.name)
            it.writeToJson(spanJson)
            jsonArray.put(spanJson)
        }
        json.put("spans", jsonArray)
        return json.toString()
    }

    override fun getRawText(source: String): String {
        val json = JSONObject(source)
        val rawText = json.getString("text")
        return rawText
    }

    override fun parseString(dest: MokaEditText, source: String) {
        val json = JSONObject(source)
        val spanArray = json.getJSONArray("spans")

        for (index in 0 until spanArray.length()) {
            val spanJson = spanArray.getJSONObject(index)
            val start = spanJson.getInt("start")
            val end = spanJson.getInt("end")
            val flag = spanJson.getInt("flag")
            val span = createSpan(spanJson, dest)
            dest.text?.setSpan(span, start, end, flag)
        }
    }
}