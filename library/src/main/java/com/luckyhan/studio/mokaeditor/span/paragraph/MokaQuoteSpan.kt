package com.luckyhan.studio.mokaeditor.span.paragraph

import android.os.Parcel
import android.text.style.QuoteSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject

class MokaQuoteSpan(
    private val mColor : Int = 0,
    private val mStripeWidth : Int = 20,
    private val mGapWidth : Int = 40)
    : QuoteSpan(Parcel.obtain().apply {
    writeInt(mColor)
    writeInt(mStripeWidth)
    writeInt(mGapWidth)
    setDataPosition(0)
}), MokaSpan {
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