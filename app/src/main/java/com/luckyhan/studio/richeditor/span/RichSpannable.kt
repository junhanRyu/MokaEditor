package com.luckyhan.studio.richeditor.span

interface RichSpannable : Copyable, Taggable {
    override fun getClosingTag() : String {
        return "</annotation>"
    }
}