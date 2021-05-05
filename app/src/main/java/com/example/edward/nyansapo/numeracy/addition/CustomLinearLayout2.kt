package com.example.edward.nyansapo.numeracy.addition

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import com.google.mlkit.vision.digitalink.Ink


class CustomLinearLayout2(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val TAG = "CustomLinearLayout2"


    private val writePaint: Paint
    private val deletePaint: Paint
    private val rubberPaint: Paint
    private val writePath: Path
    private val deletePath: Path
    private val rubberPath: Path
    var rubberOn = false


    val rectWidth = 100
    val rectHeight = 100
    var rectangle: Rect
    val radius: Float = 50f
    var centerX: Float = 0f
    var centerY: Float = 0f


    init {

        //Size for rubber
        rectangle = Rect(10, 10, rectWidth, rectHeight)

    }


    init {
        Log.d(TAG, "init: ")
        writePaint = Paint()
        writePaint.color = Color.RED
        writePaint.style = Paint.Style.STROKE
        writePaint.strokeJoin = Paint.Join.ROUND
        writePaint.strokeCap = Paint.Cap.ROUND
        writePaint.strokeWidth = 10f


        deletePaint = Paint()
        deletePaint.color = Color.WHITE
        deletePaint.style = Paint.Style.STROKE
        deletePaint.strokeJoin = Paint.Join.ROUND
        deletePaint.strokeCap = Paint.Cap.ROUND
        deletePaint.strokeWidth = 100f

        writePath = Path()
        deletePath = Path()
        setWillNotDraw(false)

        rubberPath = Path()
        rubberPaint = Paint()
        rubberPaint.color = Color.BLUE
        rubberPaint.style = Paint.Style.FILL
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent: ")
        Log.d(TAG, "onTouchEvent: event:actionMasked:${event.actionMasked}")
        addNewTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouchEvent: down")
                if (rubberOn) {
                    centerX = event.x
                    centerY = event.y
                    deletePath.moveTo(event.x, event.y)
                    writePath.moveTo(event.x, event.y)

                } else {
                    writePath.moveTo(event.x, event.y)
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {

                Log.d(TAG, "onTouchEvent: move::x:${event.x}::y:${event.y}")

                if (rubberOn) {

                    centerX = event.x
                    centerY = event.y
                    deletePath.lineTo(event.x, event.y)
                    writePath.lineTo(event.x, event.y)

                } else {
                    writePath.lineTo(event.x, event.y)
                }




                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "onTouchEvent: up")
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val displayMetrics = DisplayMetrics()
        val windowManager = this.context.getSystemService(Context.WINDOW_SERVICE) as (WindowManager)
        windowManager.getDefaultDisplay().getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        val mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        val newCanvas=Canvas()
        newCanvas.setBitmap(mBitmap)


        if (rubberOn) {
            writePaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR));

            canvas.drawPath(writePath, writePaint)
            //    canvas.drawPath(deletePath, deletePaint)

        } else {
            //    canvas.drawPath(deletePath, deletePaint)
            canvas.drawPath(writePath, writePaint)
            writePaint.setXfermode(null);
        }

        if (rubberOn) {
            //  canvas.drawRect(rectangle, rubberPaint)
            canvas.drawCircle(centerX, centerY, radius, rubberPaint)

        }

        super.onDraw(canvas)
        Log.d(TAG, "onDraw: ")
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
        writePath.reset()
        invalidate()
    }


}