package com.luckyhan.studio.richeditor.span

interface RichSpan : RichCopyable, RichTaggable {
    override fun getClosingTag() : String {
        return "</annotation>"
    }
}