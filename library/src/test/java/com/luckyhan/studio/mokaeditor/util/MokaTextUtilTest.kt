package com.luckyhan.studio.mokaeditor.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class MokaTextUtilTest {

    @Test
    fun getStartOfLine() {
        var source = "hello world!"
        assertThat(MokaTextUtil.getStartOfLine(source, 5)).isEqualTo(0)
        source = "hello world.\nthis is the test."
        assertThat(MokaTextUtil.getStartOfLine(source, 5)).isEqualTo(0)
        assertThat(MokaTextUtil.getStartOfLine(source, source.length)).isEqualTo(13)
        source = ""
        assertThat(MokaTextUtil.getStartOfLine(source, source.length)).isEqualTo(0)
        source = "\n\n\n\n\n\n\n"
        assertThat(MokaTextUtil.getStartOfLine(source, 2)).isEqualTo(2)
    }

    @Test
    fun getEndOfLine() {
        var source = "hello world!"
        assertThat(MokaTextUtil.getEndOfLine(source, 5)).isEqualTo(12)
        source = "hello world.\nthis is the test."
        assertThat(MokaTextUtil.getEndOfLine(source, 15)).isEqualTo(30)
        assertThat(MokaTextUtil.getEndOfLine(source, source.length)).isEqualTo(source.length)
        source = ""
        assertThat(MokaTextUtil.getEndOfLine(source, source.length)).isEqualTo(0)
        source = "\n\n\n\n\n\n\n"
        assertThat(MokaTextUtil.getEndOfLine(source, 2)).isEqualTo(2)
    }

    @Test
    fun getPreviousLine() {
        var source = "hello world.\nthis is the test."
        assertThat(MokaTextUtil.getPreviousLine(source, 13)).isEqualTo("hello world.")
        source = "\n\n\n\n\n\n\n"
        assertThat(MokaTextUtil.getPreviousLine(source, 1)).isEqualTo("")
        assertThat(MokaTextUtil.getPreviousLine(source, 2)).isEqualTo("")
    }
}