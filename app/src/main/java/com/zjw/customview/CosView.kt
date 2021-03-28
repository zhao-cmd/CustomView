package com.zjw.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 *  author:zjw
 *  time:2021/03/18
 *  desc:指数函数和旋转矢量
 */
class CosView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),LifecycleObserver{

    private var mAngle=10f
    private var mWidth=0f
    private var mHeight=0f
    private var mRadius=0f
    private var sineWavePath=Path()
    private lateinit var job:Job

    private var linePaint= Paint().apply {
        style=Paint.Style.STROKE
        strokeWidth=5f
        color=ContextCompat.getColor(context,R.color.white)
    }

    private var textPaint=Paint().apply {
        textSize=40f
        typeface= Typeface.DEFAULT
        color=ContextCompat.getColor(context,R.color.white)
    }

    private var smallCirclePaint=Paint().apply {
        style=Paint.Style.FILL
        color=ContextCompat.getColor(context,R.color.white)
    }

    private var circlePaint=Paint().apply {
        style=Paint.Style.STROKE
        pathEffect=DashPathEffect(floatArrayOf(10f,10f),0f)
        color=ContextCompat.getColor(context,R.color.white)
        strokeWidth=5f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth=w.toFloat()
        mHeight=h.toFloat()
        mRadius=if (w<h/2) (w/2).toFloat() else (h/4).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas.apply {
            drawLongLines(this)
            drawDashCircle(this)
            drawLongRect(this)
            drawVector(this)
            drawProjectCircle(this)
            drawSineWave(this)
        }
    }

    private fun drawLongLines(canvas: Canvas?){
        canvas?.withTranslation(mWidth/2,mHeight/2){
            drawLine(-mWidth/2,0f,mWidth/2,0f,linePaint)
            drawLine(0f,-mHeight/2,0f,mHeight/2,linePaint)
        }
        canvas?.withTranslation(mWidth/2,mHeight/4*3){
            drawLine(-mWidth/2,0f,mWidth/2,0f,linePaint)
        }
    }

    private fun drawDashCircle(canvas: Canvas?){
        canvas?.withTranslation(mWidth/2,mHeight/4*3){
            drawCircle(0f,0f,mRadius,circlePaint)
        }
    }

    private fun drawLongRect(canvas: Canvas?){
        canvas?.drawRect(100f,100f,500f,250f,linePaint)
        canvas?.drawText("指数函数和旋转矢量",120f,195f,textPaint)
    }


    private fun drawVector(canvas: Canvas?){
        canvas?.withTranslation(mWidth/2,mHeight/4*3){
            withRotation(-mAngle){
                drawLine(0f,0f,mRadius,0f,linePaint)
            }
        }
    }

    private fun drawProjectCircle(canvas: Canvas?){
        val x= mRadius*cos(mAngle.change())
        val y=mRadius*sin(mAngle.change())
        canvas?.withTranslation(mWidth/2,mHeight/2){
            drawCircle(x,0f,20f,smallCirclePaint)
        }
        canvas?.withTranslation(mWidth/2,mHeight/4*3){
            drawCircle(x,0f,20f,smallCirclePaint)
        }

        canvas?.withTranslation(mWidth/2,mHeight/4*3){
            withTranslation(x,-y){
                drawLine(0f,0f,0f,y,linePaint)
                drawLine(0f,0f,0f,-mHeight/4+ y,circlePaint)
            }
        }

    }

    private fun drawSineWave(canvas: Canvas?){
        canvas?.withTranslation(mWidth/2,mHeight/2){
            val sampleCount=50
            val dy=mHeight/2/sampleCount
            sineWavePath.reset()
            sineWavePath.moveTo(mRadius* cos(mAngle.change()),0f)
            repeat(sampleCount){
                val x=mRadius* cos(it*-0.15+mAngle.change())
                val y=-dy*it
                sineWavePath.quadTo(x.toFloat(),y,x.toFloat(),y)
            }
            drawPath(sineWavePath,linePaint)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun startRotating(){
        job=CoroutineScope(Dispatchers.Main).launch {
            while (true){
                delay(100)
                mAngle+=5f
                invalidate()
            }
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stopRotating(){
        job.cancel()
    }

    fun Float.change()=this/180* PI.toFloat()

}