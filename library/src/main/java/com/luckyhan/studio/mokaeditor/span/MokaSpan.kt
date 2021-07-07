package com.luckyhan.studio.mokaeditor.span

import android.text.NoCopySpan
import android.text.style.UpdateLayout
import org.json.JSONObject

interface MokaSpan : MokaCopyable, UpdateLayout, NoCopySpan {
    fun getSpanTypeName() : String
    fun writeToJson(json : JSONObject)
}