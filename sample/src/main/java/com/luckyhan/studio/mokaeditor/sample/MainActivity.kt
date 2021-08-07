package com.luckyhan.studio.mokaeditor.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.luckyhan.studio.mokaeditor.MokaEditText
import com.luckyhan.studio.mokaeditor.MokaSpanTool
import com.luckyhan.studio.mokaeditor.span.MokaCharacterStyle
import com.luckyhan.studio.mokaeditor.span.character.*
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaBulletSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaCheckBoxSpan
import com.luckyhan.studio.mokaeditor.span.paragraph.MokaQuoteSpan
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var spanTool: MokaSpanTool
    private lateinit var editext: MokaEditText


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("spantool", spanTool.saveInstanceState())
    }

    private val galleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d("MainActivity", "StartActivityForResult")
        if (it.resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = it.data?.data
            selectedImage?.let { uri ->
                try {
                    externalCacheDir?.let {dir->
                        val imageName = File(dir, "img" + System.currentTimeMillis())
                        val inputStream = contentResolver.openInputStream(uri)
                        inputStream?.let {
                            val outputStream = FileOutputStream(imageName)
                            val bytes = ByteArray(1024)
                            var len = 0
                            while (inputStream.read(bytes).also { len = it } != -1) {
                                outputStream.write(bytes, 0, len)
                            }
                            inputStream.close()
                            outputStream.close()
                            Log.d("MainActivity", "parent : ${dir.path} ,name : ${imageName.name}")
                            MokaImageSpan.imageDir = externalCacheDir
                            val imageSpan = MokaImageSpan(editext, imageName.name)
                            spanTool.setImageSpan(imageSpan)
                        }

                    }
                } catch (e: Exception) {
                    Log.d("MainActivity", "File error")
                }
                /*contentResolver.openFileDescriptor(selectedImage, "r").use { pfd->
                    pfd?.let{
                        Log.d("MainActivity", "pfd there")
                        val image = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
                        imageView.setImageBitmap(image)
                    }
                }*/
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editext = findViewById<MokaEditText>(R.id.editor)

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

        val undo = findViewById<Button>(R.id.undo)
        val redo = findViewById<Button>(R.id.redo)

        val image = findViewById<Button>(R.id.image)

        spanTool = MokaSpanTool(editext, lifecycleScope)
        editext.textChangeListener = spanTool

        savedInstanceState?.let{
            val spanToolParcel = savedInstanceState.getParcelable<Parcelable>("spantool")
            spanToolParcel?.let{
                spanTool.restoreSavedInstanceState(spanToolParcel)
            }
        }


        image.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            galleryActivity.launch(intent)
        }

        undo.setOnClickListener {
            spanTool.undo()
        }
        redo.setOnClickListener {
            spanTool.redo()
        }

        bullet.text = "remove"
        bullet.setOnClickListener {
            //spanTool.toggleParagraphStyleSpan(MokaBulletSpan())
            spanTool.removeCharacterSpan(MokaCharacterStyle::class.java)

        }
        checkbox.setOnClickListener {
            spanTool.toggleParagraphStyleSpan(MokaCheckBoxSpan(editext))
        }
        quote.setOnClickListener {
            spanTool.toggleParagraphStyleSpan(MokaQuoteSpan())
        }

        bold.setOnClickListener {
            spanTool.toggleCharacterStyleSpan(MokaBoldSpan())
        }
        underline.setOnClickListener {
            spanTool.toggleCharacterStyleSpan(MokaUnderlineSpan())
        }
        strikethrough.setOnClickListener {
            spanTool.toggleCharacterStyleSpan(MokaStrikethroughSpan())
        }

        sizeSmall.setOnClickListener {
            spanTool.replaceCharacterStyleSpan(MokaFontSizeSpan(0.8f))
        }
        sizeNormal.setOnClickListener {
            spanTool.replaceCharacterStyleSpan(MokaFontSizeSpan(1.0f))
        }
        sizeLarge.setOnClickListener {
            spanTool.replaceCharacterStyleSpan(MokaFontSizeSpan(1.5f))
        }

        fblue.setOnClickListener {
            spanTool.replaceCharacterStyleSpan(MokaForegroundColorSpan(Color.BLUE))
        }
        fred.setOnClickListener {
            spanTool.replaceCharacterStyleSpan(MokaForegroundColorSpan(Color.RED))
        }
        fblack.setOnClickListener {
            spanTool.replaceCharacterStyleSpan(MokaForegroundColorSpan(editext.currentTextColor))
        }

        bblue.setOnClickListener {
            spanTool.replaceCharacterStyleSpan(MokaBackgroundColorSpan(Color.parseColor("#800000ff")))
        }
        bred.setOnClickListener {
            spanTool.replaceCharacterStyleSpan(MokaBackgroundColorSpan(Color.parseColor("#80ff0000")))
        }
        bwhite.setOnClickListener {
            spanTool.replaceCharacterStyleSpan(MokaBackgroundColorSpan(Color.TRANSPARENT))
        }
    }
}