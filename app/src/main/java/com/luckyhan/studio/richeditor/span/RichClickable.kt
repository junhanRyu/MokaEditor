package com.luckyhan.studio.richeditor.span

interface RichClickable {
    var clickableLeft : Int
    var clickableRight : Int
    var clickableTop : Int
    var clickableBottom : Int
    fun onClicked()
}