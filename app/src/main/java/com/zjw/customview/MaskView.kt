package com.zjw.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlin.random.Random


/**
 *  author:zjw
 *  time:2021/03/27
 *  desc:路径和遮罩实现的简单自定义view
 */
class MaskView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val path= Path()
    private val paint=Paint().apply {
        color=ContextCompat.getColor(context,R.color.white)
    }
    private var faceX=0f
    private var faceY=0f
    private val icon=ContextCompat.getDrawable(context,R.drawable.ic_baseline_tag_faces_24)?.toBitmap(300,300)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            icon?.let { drawBitmap(it,faceX,faceY,null) }
            drawPath(path,paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply {
            when(action){
                MotionEvent.ACTION_DOWN->{
                    randomPosition()
                    path.reset()
                    path.addRect(0f,0f,width.toFloat(),height.toFloat(),Path.Direction.CW)
                    path.addCircle(x,y,300f,Path.Direction.CCW)
                }
                MotionEvent.ACTION_UP->{
                    path.reset()
                }
                MotionEvent.ACTION_MOVE->{
                    path.reset()
                    path.addRect(0f,0f,width.toFloat(),height.toFloat(),Path.Direction.CW)
                    path.addCircle(x,y,300f,Path.Direction.CCW)
                }
            }
        }
        invalidate()
        return true
    }


    private fun randomPosition(){
        faceX=Random.nextInt(width-300).toFloat()
        faceY= Random.nextInt(height-300).toFloat()
    }

}