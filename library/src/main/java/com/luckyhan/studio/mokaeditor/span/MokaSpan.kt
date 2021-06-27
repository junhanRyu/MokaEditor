package com.luckyhan.studio.mokaeditor.span

import org.json.JSONObject

interface MokaSpan : MokaCopyable {
    fun getSpanTypeName() : String
    fun writeToJson(json : JSONObject)
}