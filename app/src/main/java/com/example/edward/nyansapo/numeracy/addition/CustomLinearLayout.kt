package com.example.edward.nyansapo.numeracy.addition

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import com.google.mlkit.vision.digitalink.Ink

class CustomLinearLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val TAG = "CustomLinearLayout"


    private val mPaint: Paint
    private val mPath: Path

    init {
        Log.d(TAG, "init: ")
        mPaint = Paint()
        mPaint.color = Color.RED
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = 10f
        mPath = Path()
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(mPath, mPaint)
        super.onDraw(canvas)
        Log.d(TAG, "onDraw: ")
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent: ")
        Log.d(TAG, "onTouchEvent: event:actionMasked:${event.actionMasked}")
        addNewTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouchEvent: down")
                mPath.moveTo(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "onTouchEvent: move")
                mPath.lineTo(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "onTouchEvent: up")
            }
        }
        return true
    }

    var inkBuilder = Ink.builder()
    lateinit var strokeBuilder: Ink.Stroke.Builder

    // Call this each time there is a new event.
    fun addNewTouchEvent(event: MotionEvent) {
        val action = event.actionMasked
        val x = event.x
        val y = event.y
        var t = System.currentTimeMillis()

        // If your setup does not provide timing information, you can omit the
        // third paramater (t) in the calls to Ink.Point.create
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                strokeBuilder = Ink.Stroke.builder()
                strokeBuilder.addPoint(Ink.Point.create(x, y, t))
            }
            MotionEvent.ACTION_MOVE -> strokeBuilder!!.addPoint(Ink.Point.create(x, y, t))
            MotionEvent.ACTION_UP -> {
                strokeBuilder.addPoint(Ink.Point.create(x, y, t))
                inkBuilder.addStroke(strokeBuilder.build())
            }
            else -> {
                // Action not relevant for ink construction
            }
        }
    }


    fun clearDrawing() {
        mPath.reset()
        invalidate()
    }


}