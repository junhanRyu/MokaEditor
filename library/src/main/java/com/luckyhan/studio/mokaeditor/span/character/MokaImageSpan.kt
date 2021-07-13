package com.luckyhan.studio.mokaeditor.span.character

import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import android.util.Log
import android.widget.EditText
import com.luckyhan.studio.mokaeditor.span.MokaCharacterStyle
import com.luckyhan.studio.mokaeditor.span.MokaSpan
import org.json.JSONObject
import java.io.File
import java.lang.ref.WeakReference


class MokaImageSpan(
    private val editText: EditText,
    private val imageName: String
) : ImageSpan(editText.context, getBitmap(imageDir, imageName)), MokaCharacterStyle {

    private var mDrawableRef: WeakReference<Drawable>? = null
    private val mContainerWidth = editText.measuredWidth
    private val MIN_WIDTH = 240

    companion object {
        // this has to be initialized before used.
        var imageDir: File? = null

        fun getBitmap(dir: File?, name: String): Bitmap {
            if (dir?.exists() == true) {
                val imageFile = File(dir, name)
                if (imageFile.exists()) {
                    return BitmapFactory.decodeFile(imageFile.path)
                } else Log.d("MokaImageSpan", "no file")
            }
            return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }

    override fun getSize(
        paint: Paint, text: CharSequence?, start: Int, end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val d: Drawable? = getCachedDrawable()
        val rect: Rect = getResizedDrawableBounds(d)
        if (fm != null) {
            fm.ascent = -rect.bottom
            fm.descent = 0
            fm.top = fm.ascent
            fm.bottom = 0
        }
        return rect.right
    }

    /*override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        //super.draw(canvas, text, start, end, x, top, y, bottom, paint)

        val b = getCachedDrawable()

        b?.let{
            canvas.save()

            var transY = bottom - b.bounds.bottom
            if (mVerticalAlignment == ALIGN_BASELINE) {
                transY -= paint.fontMetricsInt.descent
            } else if (mVerticalAlignment == ALIGN_CENTER) {
                transY = top + (bottom - top) / 2 - b.bounds.height() / 2
            }

            canvas.translate(x, transY.toFloat())
            b.draw(canvas)
            canvas.restore()
        }
    }*/

    private fun getResizedDrawableBounds(d: Drawable?): Rect {
        if (d == null || d.intrinsicWidth == 0) {
            return Rect(0, 0, d!!.intrinsicWidth, d.intrinsicHeight)
        }
        val scaledHeight: Int
        if (d.intrinsicWidth < mContainerWidth) {
            // Image smaller than container's width.
            if (d.intrinsicWidth > MIN_WIDTH &&
                d.intrinsicWidth >= d.intrinsicHeight
            ) {
                // But larger than the minimum scale size, we need to scale the image to fit
                // the width of the container.
                val scaledWidth: Int = mContainerWidth
                scaledHeight = d.intrinsicHeight * scaledWidth / d.intrinsicWidth
                d.setBounds(0, 0, scaledWidth, scaledHeight)
            } else {
                // Smaller than the minimum scale size, leave it as is.
                d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            }
        } else {
            // Image is larger than the container's width, scale down to fit the container.
            val scaledWidth: Int = mContainerWidth
            scaledHeight = d.intrinsicHeight * scaledWidth / d.intrinsicWidth
            d.setBounds(0, 0, scaledWidth, scaledHeight)
        }
        return d.bounds
    }

    private fun getCachedDrawable(): Drawable? {
        val wr: WeakReference<Drawable>? = mDrawableRef
        var d: Drawable? = null
        if (wr != null) {
            d = wr.get()
        }
        if (d == null) {
            d = drawable
            mDrawableRef = WeakReference<Drawable>(d)
        }
        return d
    }

    override fun copy(): MokaSpan {
        return MokaImageSpan(editText, imageName)
    }

    override fun getSpanTypeName(): String {
        return "image"
    }

    override fun writeToJson(json: JSONObject) {
        json.put("image", imageName)
    }
}