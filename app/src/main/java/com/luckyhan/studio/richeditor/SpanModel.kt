package com.luckyhan.studio.richeditor

import com.luckyhan.studio.richeditor.span.RichSpannable

data class SpanModel(val span : RichSpannable, val flag : Int, val start : Int, val end : Int)