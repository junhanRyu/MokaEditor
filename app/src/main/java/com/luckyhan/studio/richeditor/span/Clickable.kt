package com.luckyhan.studio.richeditor.span

interface Clickable {
    var clickableLeft : Int
    var clickableRight : Int
    var clickableTop : Int
    var clickableBottom : Int
    fun onClicked()
}