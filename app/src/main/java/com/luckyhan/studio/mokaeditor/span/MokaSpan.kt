package com.luckyhan.studio.mokaeditor.span

interface MokaSpan : MokaCopyable, MokaTaggable {
    override fun getClosingTag() : String {
        return "</annotation>"
    }
}