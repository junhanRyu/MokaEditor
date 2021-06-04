package com.luckyhan.studio.richeditor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editor = findViewById<RichEditText>(R.id.editor)
        val bullet = findViewById<Button>(R.id.bullet)
        val checkbox = findViewById<Button>(R.id.checkbox)
        val bold = findViewById<Button>(R.id.bold)

        bullet.setOnClickListener{
            editor.toggleBullet()
        }
        checkbox.setOnClickListener{
            editor.toggleCheckBox()
        }
        bold.setOnClickListener{
            editor.toggleBold()
        }
    }

}