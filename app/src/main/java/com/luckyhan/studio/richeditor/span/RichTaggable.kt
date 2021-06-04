package com.luckyhan.studio.richeditor.span

interface RichTaggable {
    fun getOpeningTag() : String
    fun getClosingTag() : String
}