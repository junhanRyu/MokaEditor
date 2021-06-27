package com.luckyhan.studio.mokaeditor

import android.util.Log
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import com.luckyhan.studio.mokaeditor.span.character.*
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaBulletSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaCheckBoxSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaQuoteSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaStrikeThroughParagraphSpan
import org.json.JSONObject
import java.lang.UnsupportedOperationException

class DefaultMokaSpanParser : MokaSpanParser(){
    override fun createSpan(sourceJson : JSONObject, dest : MokaEditText) : MokaSpan {
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
            else->{
                throw UnsupportedOperationException("Not supported span!")
            }
        }
    }
}