package com.zjw.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *  author:zjw
 *  time:2021/03/28
 *  desc:气泡效果
 */
class BubbleSurFaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        pathEffect= DiscretePathEffect(30f,20f)
    }
    private val colors = arrayOf(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW)

    private data class Bubble(val x: Float, val y: Float, val color: Int, var mRadius: Float)

    private val bubbleList = mutableListOf<Bubble>()

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x ?: 0f
        val y = event?.y ?: 0f
        bubbleList.add(Bubble(x, y, colors.random(), 1f))
        if (bubbleList.size > 30) bubbleList.removeAt(0)
        return super.onTouchEvent(event)
    }

    init {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (holder.surface.isValid) {
                    val canvas = holder.lockCanvas()
                    canvas.drawColor(Color.BLACK)
                    bubbleList.toList().filter { it.mRadius < 3000 }.forEach {
                        paint.color=it.color
                        canvas.drawCircle(it.x,it.y,it.mRadius,paint)
                        it.mRadius+=10f
                    }
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }


}