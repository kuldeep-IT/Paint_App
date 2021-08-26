package com.peerbitskuldeep.paintdemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawPath: CustomPath? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null

    //draw
    private val mPaths = ArrayList<CustomPath>()

    //undo
    private val mUndoPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    fun onClickUndo()
    {
        if (mPaths.size > 0)
        {
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
            invalidate() //invalidate() means 'redraw on screen' and results to a call of the view's onDraw() method.
        }
    }

    private fun setUpDrawing() {

        mDrawPaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
//        mBrushSize = 20.toFloat()

    }

//    This is called during layout when the size of this view has changed.
//    If you were just added to the view hierarchy, you're called with the old values of 0.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mCanvasBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        //first set mPaths.add(mDrawPath!!) in ACTION_UO
        for (path in mPaths)
        {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas?.drawPath(path, mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty)
        {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val touchX = event?.x
        val touchY = event?.y

        when(event?.action)
        {
            MotionEvent.ACTION_DOWN -> {

                Toast.makeText(this.context, "ACTION_DOWN",Toast.LENGTH_SHORT).show()

                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                if (touchY != null) {
                    if (touchX != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {

                Toast.makeText(this.context, "ACTION_MOVE",Toast.LENGTH_SHORT).show()

                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath?.lineTo(touchX,touchY)
                    }
                }

            }


            MotionEvent.ACTION_UP -> {

                mPaths.add(mDrawPath!!)

                Toast.makeText(this.context, "ACTION_UP",Toast.LENGTH_SHORT).show()
                mDrawPath = CustomPath(color, mBrushSize)

            }
            else -> return false
        }

        invalidate()

        return true
    }

    fun setSizeForBrush(newSize: Float)
    {
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        newSize,
        resources.displayMetrics)

        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String)
    {
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    internal inner class CustomPath(var color: Int,
                                    var brushThickness: Float): Path()
    {


                                    }

}