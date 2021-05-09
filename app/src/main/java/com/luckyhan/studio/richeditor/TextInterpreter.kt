package com.luckyhan.studio.richeditor

import android.text.Editable

object TextInterpreter {
    enum class Case{
        NEW_LINE_INSERTED,
        NEW_LINE_REMOVED,
        TEXT_INSERTED,
        TEXT_REMOVED,
        IGNORE
    }

    fun interpretTextBeforeChange(s : CharSequence, start : Int, count : Int, after : Int) : Case{
        val diff = after - count
        return if(diff == -1){
            if(s[start+count-1] == '\n') Case.NEW_LINE_REMOVED
            else Case.IGNORE
        }else if(diff < -1 || diff == 0){
            Case.TEXT_REMOVED
        }else{
            Case.IGNORE
        }
    }

    fun interpretTextAfterChange(s : CharSequence, start : Int, before : Int, count : Int) : Case{
        val diff = count - before
        return if(diff == 1){
            if(s[start+count-1] == '\n') Case.NEW_LINE_INSERTED
            else Case.IGNORE
        }else if(diff > 1 || diff == 0){
            Case.TEXT_INSERTED
        }else {
            Case.IGNORE
        }
    }
}