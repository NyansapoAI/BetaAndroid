package com.example.edward.nyansapo.numeracy.word_problem

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatTextView
import com.edward.nyansapo.R

class RubberTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    var mPaint: Paint
    val rectWidth = 100
    val rectHeight = 100
    var rectangle: Rect

    init {
        mPaint = Paint()
        mPaint.textSize = 25f
        mPaint.color = -0xffff01
        //Size for image
        rectangle = Rect(10, 10, rectWidth, rectHeight)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent: ")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouchEvent: action down")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "onTouchEvent: action move")
                //   if (canImageMove == true) {
                Log.d(TAG, "onTouchEvent: can image move")
                rectangle.left = event.x.toInt()
                rectangle.top = event.y.toInt()
                rectangle.right = event.x.toInt() + rectWidth
                rectangle.bottom = event.y.toInt() + rectHeight
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "onTouchEvent: action up")
            }
        }
        return true
    }

    public override fun onDraw(canvas: Canvas) {
        Log.d(TAG, "onDraw: ")
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.CYAN
        canvas.drawRect(rectangle, paint)

    }

    companion object {
        private const val TAG = "RubberTextView"
        const val TOUCH_MODE_TAP = 1
        const val TOUCH_MODE_DOWN = 2
    }


}