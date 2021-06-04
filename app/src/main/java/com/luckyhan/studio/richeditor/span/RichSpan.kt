package com.luckyhan.studio.richeditor.span

interface RichSpan : Copyable, Taggable {
    override fun getClosingTag() : String {
        return "</annotation>"
    }
}