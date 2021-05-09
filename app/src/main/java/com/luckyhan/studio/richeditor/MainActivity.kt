package com.luckyhan.studio.richeditor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editor = findViewById<RichEditor>(R.id.editor)
        val add = findViewById<Button>(R.id.add)
        val remove = findViewById<Button>(R.id.remove)

        add.setOnClickListener{
            editor.addBullet()
        }

        remove.setOnClickListener{
            editor.addBold()
        }
    }

}