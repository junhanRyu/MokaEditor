package com.luckyhan.studio.richeditor.span

interface Taggable {
    fun getOpeningTag() : String
    fun getClosingTag() : String
}