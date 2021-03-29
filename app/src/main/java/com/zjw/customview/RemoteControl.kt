package com.zjw.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withTranslation
import kotlin.math.min


/**
 *  author:zjw
 *  time:2021/03/29
 *  desc:遥控器自定义view
 */
class RemoteControl @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#BEBEBE")
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 120F
        color = Color.parseColor("#000000")
        textAlign = Paint.Align.CENTER
    }

    private val mClickPaint = Paint().apply {
        color = Color.parseColor("#000000")
    }

    private val mPathLeft = Path()
    private val mPathTop = Path()
    private val mPathRight = Path()
    private val mPathBottom = Path()
    private val mPathCenter = Path()

    private var mAllRegion=Region()

    private val mRegionLeft = Region()
    private val mRegionTop = Region()
    private val mRegionRight = Region()
    private val mRegionBottom = Region()
    private val mRegionCenter = Region()

    private val mInitSweepAngle = 0f
    private val mBigSweepAngle = 84f
    private val mLittleSweepAngle = 82f

    private val mBigMarginAngle = 0f
    private val mLittleMarginAngle = 0f

    private val mList = ArrayList<Region>()

    private var mRectFBig=RectF()
    private var mRectFLittle=RectF()

    private var mRadius = 0

    private val LEFT = 0
    private val TOP = 1
    private val RIGHT = 2
    private val BOTTOM = 3
    private val CENTER = 4

    private val mClickFlag = -1

    private var mWidth = 0

    private val mCurX = 0
    private var mCurY: Int = 0

    fun initPath() {
        mList.clear()
        mPathRight.addArc(mRectFBig, -40F, mBigSweepAngle)
        mPathRight.arcTo(mRectFLittle, 40F, -mLittleSweepAngle)
        mPathRight.close()
        // 计算right的区域
        mRegionRight.setPath(mPathRight, mAllRegion)
        mList.add(mRegionRight)

        // 初始化bottom路径
        mPathBottom.addArc(mRectFBig, 50F, mBigSweepAngle)
        mPathBottom.arcTo(mRectFLittle, 130F, -mLittleSweepAngle)
        mPathBottom.close()

        // 计算bottom的区域
        mRegionBottom.setPath(mPathBottom, mAllRegion)
        mList.add(mRegionBottom)

        // 初始化left路径
        mPathLeft.addArc(mRectFBig, 140F, mBigSweepAngle)
        mPathLeft.arcTo(mRectFLittle, 220F, -mLittleSweepAngle)
        mPathLeft.close()
        // 计算left的区域
        mRegionLeft.setPath(mPathLeft, mAllRegion)
        mList.add(mRegionLeft)

        // 初始化top路径
        mPathTop.addArc(mRectFBig, 230F, mBigSweepAngle)
        mPathTop.arcTo(mRectFLittle, 310F, -mLittleSweepAngle)
        mPathTop.close()
        // 计算top的区域
        mRegionTop.setPath(mPathTop, mAllRegion)
        mList.add(mRegionTop)

        // 初始化center路径
        mPathCenter.addCircle(0f, 0f, mRadius.toFloat(), Path.Direction.CW)
        mPathCenter.close()
        // 计算center的区域
        mRegionCenter.setPath(mPathCenter, mAllRegion)
        mList.add(mRegionCenter)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = min(measuredWidth, measuredHeight) / 4 * 3
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mAllRegion = Region(-mWidth, -mWidth, mWidth, mWidth)
        mRectFBig = RectF(
            (-mWidth / 2).toFloat(),
            (-mWidth / 2).toFloat(),
            (mWidth / 2).toFloat(),
            (mWidth / 2).toFloat()
        )
        mRectFLittle = RectF(
            (-mWidth / 3).toFloat(), (-mWidth / 3).toFloat(), (mWidth / 3).toFloat(),
            (mWidth / 3).toFloat()
        )
        mRadius = mWidth / 4
        initPath()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.withTranslation((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat()) {
            drawPath(mPathRight, mPaint)
            drawPath(mPathLeft, mPaint)
            drawPath(mPathBottom, mPaint)
            drawPath(mPathTop, mPaint)
            drawPath(mPathCenter, mPaint)
        }
    }

}