package com.luckyhan.studio.mokaeditor.span

interface MokaClickable {
    var clickableLeft : Int
    var clickableRight : Int
    var clickableTop : Int
    var clickableBottom : Int
    fun onClicked()
}