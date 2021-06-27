package com.luckyhan.studio.mokaeditor.span.paragraph

import android.graphics.Color
import android.os.Parcel
import android.text.NoCopySpan
import android.text.style.QuoteSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaQuoteSpan(
    private val mColor : Int = Color.GREEN,
    private val mStripeWidth : Int = 20,
    private val mGapWidth : Int = 40)
    : QuoteSpan(Parcel.obtain().apply {
    writeInt(mColor)
    writeInt(mStripeWidth)
    writeInt(mGapWidth)
    setDataPosition(0)
}), MokaSpan, NoCopySpan {
    override fun copy(): MokaSpan {
        return MokaQuoteSpan()
    }

    override fun getSpanTypeName(): String {
        return "quote"
    }

    override fun writeToJson(json: JSONObject) {
        return
    }
}