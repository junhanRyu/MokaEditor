package com.luckyhan.studio.richeditor

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText

class MyEditText : AppCompatEditText {
    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection {
        return object : InputConnectionWrapper(super.onCreateInputConnection(outAttrs), true){
            override fun sendKeyEvent(event: KeyEvent?): Boolean {
                if(event?.action == KeyEvent.ACTION_DOWN){
                    when(event.keyCode){
                        KeyEvent.KEYCODE_DEL->{
                            //catch delete key
                        }
                        KeyEvent.KEYCODE_ENTER->{
                            //catch enter key
                        }
                    }

                }
                return super.sendKeyEvent(event)
            }
        }
    }
}