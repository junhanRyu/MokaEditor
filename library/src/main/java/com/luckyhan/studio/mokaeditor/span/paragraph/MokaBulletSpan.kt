package com.luckyhan.studio.mokaeditor.span.paragraph

import android.os.Parcel
import android.text.NoCopySpan
import android.text.style.BulletSpan
import com.luckyhan.studio.mokaeditor.span.MokaSpan

class MokaBulletSpan(private val mGapWidth : Int = 40, private val mColor : Int = 0, private val mRadius : Int = 8) : BulletSpan(Parcel.obtain().apply {
    writeInt(mGapWidth)
    writeInt(mColor)
    writeInt(1)
    writeInt(mRadius)
    setDataPosition(0)
}), MokaSpan, NoCopySpan {
    override fun copy(): MokaSpan {
        return MokaBulletSpan()
    }

    override fun getSpanTypeName(): String {
        return "bullet"
    }

}