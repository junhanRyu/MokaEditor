package com.luckyhan.studio.richeditor

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

object DisplayUnitUtil {
    fun getPxFromDp(context : Context, dp : Float) : Int{
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).roundToInt())
    }
}