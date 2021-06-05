package com.luckyhan.studio.richeditor.span.paragraph

import android.graphics.Color
import android.os.Parcel
import android.text.NoCopySpan
import android.text.style.QuoteSpan
import com.luckyhan.studio.richeditor.span.RichSpan

class RichQuoteSpan(
    private val mColor : Int = Color.GREEN,
    private val mStripeWidth : Int = 20,
    private val mGapWidth : Int = 40)
    : QuoteSpan(Parcel.obtain().apply {
    writeInt(mColor)
    writeInt(mStripeWidth)
    writeInt(mGapWidth)
    setDataPosition(0)
}), RichSpan, NoCopySpan {
    override fun copy(): RichSpan {
        return RichQuoteSpan()
    }

    override fun getOpeningTag(): String {
        return "<annotation type=\"quote\">"
    }

}