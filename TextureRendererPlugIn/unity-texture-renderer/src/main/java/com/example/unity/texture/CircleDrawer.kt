package com.example.unity.texture

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.Surface
import java.util.Random

class CircleDrawer(private val width: Int, private val height: Int) {

    private val mRandom = Random()
    private val mPaint = Paint()
    private val mRect = Rect(0, 0, width, height)

    fun draw(surface: Surface) {
        val c = surface.lockCanvas(mRect)
        with(mRandom) {
            mPaint.color = Color.argb(255, nextInt(255), nextInt(255), nextInt(255))
            c.drawCircle(
                nextInt(width).toFloat(),
                nextInt(height).toFloat(),
                nextInt(100).toFloat(),
                mPaint
            )
        }
        surface.unlockCanvasAndPost(c)
    }
}