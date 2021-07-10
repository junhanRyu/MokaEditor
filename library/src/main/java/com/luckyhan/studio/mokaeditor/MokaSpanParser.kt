package com.luckyhan.studio.mokaeditor

import android.text.Spannable
import android.util.Log
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONArray
import org.json.JSONObject

interface MokaSpanParser {

    fun getString(spannable: Spannable): String

    fun getRawText(source : String) : String

    fun createSpan(sourceJson : JSONObject, dest : MokaEditText) : MokaSpan

    fun parseString(dest: MokaEditText, source : String)
}