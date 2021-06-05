package com.luckyhan.studio.richeditor

import android.graphics.Color
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
        val quote = findViewById<Button>(R.id.quote)

        val bold = findViewById<Button>(R.id.bold)
        val strikethrough = findViewById<Button>(R.id.strikethrough)
        val underline = findViewById<Button>(R.id.underline)

        val sizeSmall = findViewById<Button>(R.id.sizeSmall)
        val sizeNormal = findViewById<Button>(R.id.sizeNormal)
        val sizeLarge = findViewById<Button>(R.id.sizeLarge)

        val fblue = findViewById<Button>(R.id.fblue)
        val fblack = findViewById<Button>(R.id.fblack)
        val fred = findViewById<Button>(R.id.fred)

        val bblue = findViewById<Button>(R.id.b_blue)
        val bwhite = findViewById<Button>(R.id.b_white)
        val bred = findViewById<Button>(R.id.b_red)



        val spanTool = RichSpanTool(editor)

        bullet.setOnClickListener{
            spanTool.toggleBullet()
        }
        checkbox.setOnClickListener{
            spanTool.toggleCheckBox()
        }
        quote.setOnClickListener{
            spanTool.toggleQuote()
        }

        bold.setOnClickListener{
            spanTool.toggleBold()
        }
        underline.setOnClickListener{
            spanTool.toggleUnderline()
        }
        strikethrough.setOnClickListener{
            spanTool.toggleStrikethrough()
        }

        sizeSmall.setOnClickListener{
            spanTool.setFontSize(0.5f)
        }
        sizeNormal.setOnClickListener{
            spanTool.setFontSize(1.0f)
        }
        sizeLarge.setOnClickListener{
            spanTool.setFontSize(1.5f)
        }

        fblue.setOnClickListener{
            spanTool.setForegroundColor(Color.BLUE)
        }
        fred.setOnClickListener{
            spanTool.setForegroundColor(Color.RED)
        }
        fblack.setOnClickListener{
            spanTool.setForegroundColor(Color.BLACK)
        }

        bblue.setOnClickListener{
            spanTool.setBackgroundColor(Color.BLUE)
        }
        bred.setOnClickListener{
            spanTool.setBackgroundColor(Color.RED)
        }
        bwhite.setOnClickListener{
            spanTool.setBackgroundColor(Color.WHITE)
        }


    }

}