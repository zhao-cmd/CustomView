package com.zjw.customview

import android.animation.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import kotlin.math.sqrt


/**
 *  author:zjw
 *  time:2021/03/31
 *  desc:小清新风格的加载自定义view
 */
class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 固定方块 & 移动方块变量
    private lateinit var mfixedBlocks: Array<fixedBlock?>
    private var mMoveBlock: MoveBlock? = null

    // 方块属性（下面会详细介绍）
    private var halfBlockWidth = 0f
    private var blockInterval = 0f
    private val mPaint=Paint().apply {
        flags=Paint.ANTI_ALIAS_FLAG
        style=Paint.Style.FILL
        //color=blockColor
    }

    private var isClockWise = false
    private var initPosition = 0
    private var mCurrEmptyPosition = 0
    private var lineNumber = 0
    private var blockColor = 0

    // 方块的圆角半径
    private var moveBlockAngle = 0f
    private var fixBlockAngle = 0f

    // 动画属性
    private var mRotateDegree = 0f
    private var mAllowRoll = false
    private var isMoving = false
    private var moveSpeed = 250

    // 动画插值器（默认 = 线性）
    private var moveInterpolator: Interpolator? = null
    private var mAnimatorSet: AnimatorSet? = null

    init {
        // 步骤1：初始化动画属性
        initAttrs(context, attrs);

        // 步骤2：初始化自定义View
        initBlocks(initPosition)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        // 控件资源名称
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.LoadingView)
        // 方块行数量(最少3行)
        lineNumber = typedArray.getInteger(R.styleable.LoadingView_lineNumber, 3)
        if (lineNumber < 3) {
            lineNumber = 3
        }
        // 半个方块的宽度（dp）
        halfBlockWidth = typedArray.getDimension(R.styleable.LoadingView_halfBlockWidth, 30f)
        // 方块间隔宽度（dp）
        blockInterval = typedArray.getDimension(R.styleable.LoadingView_blockInterval, 10f)
        // 移动方块的圆角半径
        moveBlockAngle = typedArray.getFloat(R.styleable.LoadingView_moveBlockAngle, 10f)
        // 固定方块的圆角半径
        fixBlockAngle = typedArray.getFloat(R.styleable.LoadingView_fixBlockAngle, 30f)
        // 通过设置两个方块的圆角半径使得二者不同可以得到更好的动画效果哦
        // 方块颜色（使用十六进制代码，如#333、#8e8e8e）
        val defaultColor = context.resources.getColor(R.color.colorAccent) // 默认颜色
        blockColor = typedArray.getColor(R.styleable.LoadingView_blockColor, defaultColor)
        // 移动方块的初始位置（即空白位置）
        initPosition = typedArray.getInteger(R.styleable.LoadingView_initPosition, 0)
        // 由于移动方块只能是外部方块，所以这里需要判断方块是否属于外部方块
        if (isInsideTheRect(initPosition, lineNumber)) {
            initPosition = 0
        }
        // 动画方向是否 = 顺时针旋转
        isClockWise = typedArray.getBoolean(R.styleable.LoadingView_isClockWise, true)
        // 移动方块的移动速度
        // 注：不建议使用者将速度调得过快
        // 因为会导致ValueAnimator动画对象频繁重复的创建，存在内存抖动
        moveSpeed = typedArray.getInteger(R.styleable.LoadingView_moveSpeed, 250)
        // 设置移动方块动画的插值器
        val moveInterpolatorResId = typedArray.getResourceId(
            R.styleable.LoadingView_moveInterpolator,
            android.R.anim.linear_interpolator
        )
        moveInterpolator = AnimationUtils.loadInterpolator(context, moveInterpolatorResId)
        // 当方块移动后，需要实时更新的空白方块的位置
        mCurrEmptyPosition = initPosition
        // 释放资源
        typedArray.recycle()
    }

    /**
     *判断方块是否在内部
     */
    private fun isInsideTheRect(pos: Int, lineCount: Int):Boolean {
        return when {
            pos<lineCount -> {
                false
            }
            pos>(lineCount*lineCount-1-lineCount) -> {
                false
            }
            (pos+1)%lineCount==0 -> {
                false
            }
            pos%lineCount==0 -> {
                false
            }
            else -> true
        }
    }




    private fun initBlocks(initPosition:Int){
        mfixedBlocks= arrayOfNulls<fixedBlock>(lineNumber*lineNumber)


        for (i in mfixedBlocks.indices){
            mfixedBlocks[i]= fixedBlock()
            mfixedBlocks[i]?.index=i
            mfixedBlocks[i]?.isShow = initPosition !== i
            mfixedBlocks[i]?.rectF = RectF()
        }

        mMoveBlock= MoveBlock()
        mMoveBlock!!.rectF= RectF()
        mMoveBlock!!.isShow=false

        relateOuterBlock(mfixedBlocks, isClockWise)
    }

    private fun relateOuterBlock(fixedBlocks: Array<fixedBlock?>, isClockwise: Boolean) {
        val lineCount = sqrt(fixedBlocks.size.toDouble()).toInt()
        // 关联第1行
        for (i in 0 until lineCount) {
            // 位于最左边
            when {
                i % lineCount == 0 -> {
                    fixedBlocks[i]?.next =
                        if (isClockwise) fixedBlocks.get(i + lineCount) else fixedBlocks.get(i + 1)
                    // 位于最右边
                }
                (i + 1) % lineCount == 0 -> {
                    fixedBlocks[i]?.next =
                        if (isClockwise) fixedBlocks.get(i - 1) else fixedBlocks.get(i + lineCount)
                    // 中间
                }
                else -> {
                    fixedBlocks[i]?.next =
                        if (isClockwise) fixedBlocks.get(i - 1) else fixedBlocks.get(i + 1)
                }
            }
        }
        // 关联最后1行
        for (i in (lineCount - 1) * lineCount until lineCount * lineCount) {
            // 位于最左边
            when {
                i % lineCount == 0 -> {
                    fixedBlocks[i]?.next =
                        if (isClockwise) fixedBlocks.get(i + 1) else fixedBlocks.get(i - lineCount)
                    // 位于最右边
                }
                (i + 1) % lineCount == 0 -> {
                    fixedBlocks[i]?.next =
                        if (isClockwise) fixedBlocks.get(i - lineCount) else fixedBlocks.get(i - 1)
                    // 中间
                }
                else -> {
                    fixedBlocks[i]?.next =
                        if (isClockwise) fixedBlocks.get(i + 1) else fixedBlocks.get(i - 1)
                }
            }
        }

        //第一列
        run {
            var i = 1 * lineCount
            while (i <= (lineCount - 1) * lineCount) {

                // 若是第1列最后1个
                if (i == (lineCount - 1) * lineCount) {
                    fixedBlocks[i]?.next =
                        if (isClockwise) fixedBlocks[i + 1] else fixedBlocks[i - lineCount]
                    i += lineCount
                    continue
                }
                fixedBlocks[i]?.next = if (isClockwise) fixedBlocks[i + lineCount] else fixedBlocks[i - lineCount]
                i += lineCount
            }
        }

        //最后一列
        run {
            var i = 2 * lineCount - 1
            while (i <= lineCount * lineCount - 1) {

                // 若是最后1列最后1个
                if (i == lineCount * lineCount - 1) {
                    fixedBlocks[i]?.next =
                        if (isClockwise) fixedBlocks[i - lineCount] else fixedBlocks[i - 1]
                    i += lineCount
                    continue
                }
                fixedBlocks[i]?.next =
                    if (isClockwise) fixedBlocks[i - lineCount] else fixedBlocks[i + lineCount]
                i += lineCount
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val measureWidth=measuredWidth
        val measureHeight=measuredHeight

        val cx=measureWidth/2
        val cy=measureHeight/2

        fixedBlockPosition(mfixedBlocks,cx,cy,blockInterval,halfBlockWidth)
        moveBlockPosition(mfixedBlocks,mMoveBlock,initPosition,isClockWise)

    }

    /**
     * 设置固定方块位置
     */
    private fun fixedBlockPosition(
        fixedBlocks: Array<fixedBlock?>,
        cx: Int,
        cy: Int,
        dividerWidth: Float,
        halfSquareWidth: Float
    ) {
        // 1. 确定第1个方块的位置
        // 分为2种情况：行数 = 偶 / 奇数时
        val squareWidth = halfSquareWidth * 2
        val lineCount = sqrt(fixedBlocks.size.toDouble()).toInt()
        var firstRectLeft = 0f
        var firstRectTop = 0f

        // 情况1：当行数 = 偶数时
        if (lineCount % 2 == 0) {
            val squareCountInAline = lineCount / 2
            val diviCountInAline = squareCountInAline - 1
            val firstRectLeftTopFromCenter =
                squareCountInAline * squareWidth + diviCountInAline * dividerWidth + dividerWidth / 2
            firstRectLeft = cx - firstRectLeftTopFromCenter
            firstRectTop = cy - firstRectLeftTopFromCenter

            // 情况2：当行数 = 奇数时
        } else {
            val squareCountInAline = lineCount / 2
            val firstRectLeftTopFromCenter =
                squareCountInAline * squareWidth + squareCountInAline * dividerWidth + halfSquareWidth
            firstRectLeft = cx - firstRectLeftTopFromCenter
            firstRectTop = cy - firstRectLeftTopFromCenter
            firstRectLeft = cx - firstRectLeftTopFromCenter
            firstRectTop = cy - firstRectLeftTopFromCenter
        }

        // 2. 确定剩下的方块位置
        // 思想：把第一行方块位置往下移动即可
        // 通过for循环确定：第一个for循环 = 行，第二个 = 列
        for (i in 0 until lineCount) { //行
            for (j in 0 until lineCount) { //列
                if (i == 0) {
                    if (j == 0) {
                        fixedBlocks[0]?.rectF!![firstRectLeft, firstRectTop, firstRectLeft + squareWidth] =
                            firstRectTop + squareWidth
                    } else {
                        val currIndex = i * lineCount + j
                        fixedBlocks[currIndex]?.rectF!!.set(fixedBlocks[currIndex - 1]?.rectF)
                        fixedBlocks[currIndex]?.rectF!!.offset(dividerWidth + squareWidth, 0F)
                    }
                } else {
                    val currIndex = i * lineCount + j
                    fixedBlocks[currIndex]?.rectF!!.set(fixedBlocks[currIndex - lineCount]?.rectF)
                    fixedBlocks[currIndex]?.rectF!!.offset(0F, dividerWidth + squareWidth)
                }
            }
        }
    }

    /**
     * 设置移动方块的位置
     */
    private fun moveBlockPosition(
        fixedBlocks: Array<fixedBlock?>,
        moveBlock: MoveBlock?, initPosition: Int, isClockwise: Boolean
    ) {

        // 移动方块位置 = 设置初始的空出位置 的下一个位置（next）
        // 下一个位置 通过 连接的外部方块位置确定
        val fixedBlock = fixedBlocks[initPosition]
        moveBlock?.rectF!!.set(fixedBlock?.next!!.rectF)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (i in mfixedBlocks.indices){
            if (mfixedBlocks[i]?.isShow!!){
                canvas?.drawRoundRect(mfixedBlocks[i]?.rectF!!,fixBlockAngle,fixBlockAngle,mPaint)
            }
        }
        if (mMoveBlock!!.isShow){
            canvas?.rotate(if (isClockWise) mRotateDegree else -mRotateDegree,
                mMoveBlock!!.cx, mMoveBlock!!.cy)
            canvas?.drawRoundRect(mMoveBlock!!.rectF!!,moveBlockAngle,moveBlockAngle,mPaint)
        }

    }

    /**
     * 步骤5：启动动画
     */
    fun startMoving() {
        // 1. 根据标志位 & 视图是否可见确定是否需要启动动画 此处设置是为了方便手动 & 自动停止动画
        if (isMoving || visibility != VISIBLE) {
            return
        }

        // 设置标记位：以便是否停止动画
        isMoving = true
        mAllowRoll = true
        // 2. 获取固定方块当前的空位置，即移动方块当前位置
        val currEmptyfixedBlock = mfixedBlocks[mCurrEmptyPosition]
        // 3. 获取移动方块的到达位置，即固定方块当前空位置的下1个位置
        val movedBlock = currEmptyfixedBlock?.next
        // 4. 设置方块动画 = 移动方块平移 + 旋转
        // 原理：设置平移动画（Translate） + 旋转动画（Rotate），最终通过组合动画（AnimatorSet）组合起来
        // 4.1 设置平移动画：createTranslateValueAnimator（） ->>关注1
        mAnimatorSet = AnimatorSet()
        // 平移路径 = 初始位置 - 到达位置
        val translateConrtroller = currEmptyfixedBlock?.let {
            createTranslateValueAnimator(
                it,
                movedBlock
            )
        }
        // 4.2 设置旋转动画：createMoveValueAnimator(（）->>关注3
        val moveConrtroller = createMoveValueAnimator()
        // 4.3 将两个动画组合起来 设置移动的插值器
        mAnimatorSet?.let {
            it.interpolator=moveInterpolator
            it.playTogether(translateConrtroller,moveConrtroller)
            it.addListener(object : AnimatorListenerAdapter(){
                // 动画开始时进行一些设置
                override fun onAnimationStart(animation: Animator) {
                    // 每次动画开始前都需要更新移动方块的位置 ->>关注4
                    updateMoveBlock()
                    // 让移动方块的初始位置的下个位置也隐藏 = 两个隐藏的方块
                    mfixedBlocks[mCurrEmptyPosition]?.next!!.isShow = false
                    // 通过标志位将移动的方块显示出来
                    mMoveBlock!!.isShow = true
                }

                // 结束时进行一些设置
                override fun onAnimationEnd(animation: Animator) {
                    isMoving = false
                    mfixedBlocks[mCurrEmptyPosition]?.isShow = true
                    mCurrEmptyPosition = mfixedBlocks[mCurrEmptyPosition]?.next!!.index
                    // 将移动的方块隐藏
                    mMoveBlock!!.isShow = false
                    // 通过标志位判断动画是否要循环播放
                    if (mAllowRoll) {
                        startMoving()
                    }
                }
            })

            it.start()
        }


    }

    /**
     * 设置平移动画
     */
    private fun createTranslateValueAnimator(
        currEmptyfixedBlock: fixedBlock,
        moveBlock: fixedBlock?
    ): ValueAnimator {
        var startAnimValue = 0f
        var endAnimValue = 0f
        var left: PropertyValuesHolder? = null
        var top: PropertyValuesHolder? = null
        // 1. 设置移动速度
        val valueAnimator = ValueAnimator().setDuration(moveSpeed.toLong())
        // 2. 设置移动方向
        // 情况分为：4种，分别是移动方块向左、右移动 和 上、下移动
        // 注：需考虑 旋转方向（isClock_Wise），即顺逆时针
        if (isNextRollLeftOrRight(currEmptyfixedBlock, moveBlock)) {

            // 情况1：顺时针且在第一行 / 逆时针且在最后一行时，移动方块向右移动
            if (isClockWise && currEmptyfixedBlock.index > moveBlock!!.index || !isClockWise && currEmptyfixedBlock.index > moveBlock!!.index) {
                startAnimValue = moveBlock.rectF!!.left
                endAnimValue = moveBlock.rectF!!.left + blockInterval

                // 情况2：顺时针且在最后一行 / 逆时针且在第一行，移动方块向左移动
            } else if (isClockWise && currEmptyfixedBlock.index < moveBlock!!.index
                || !isClockWise && currEmptyfixedBlock.index < moveBlock!!.index
            ) {
                startAnimValue = moveBlock.rectF!!.left
                endAnimValue = moveBlock.rectF!!.left - blockInterval
            }

            // 设置属性值
            left = PropertyValuesHolder.ofFloat("left", startAnimValue, endAnimValue)
            valueAnimator.setValues(left)
        } else {
            // 情况3：顺时针且在最左列 / 逆时针且在最右列，移动方块向上移动
            if (isClockWise && currEmptyfixedBlock.index < moveBlock!!.index
                || !isClockWise && currEmptyfixedBlock.index < moveBlock!!.index
            ) {
                startAnimValue = moveBlock.rectF!!.top
                endAnimValue = moveBlock.rectF!!.top - blockInterval

                // 情况4：顺时针且在最右列 / 逆时针且在最左列，移动方块向下移动
            } else if (isClockWise && currEmptyfixedBlock.index > moveBlock!!.index
                || !isClockWise && currEmptyfixedBlock.index > moveBlock!!.index
            ) {
                startAnimValue = moveBlock.rectF!!.top
                endAnimValue = moveBlock.rectF!!.top + blockInterval
            }

            // 设置属性值
            top = PropertyValuesHolder.ofFloat("top", startAnimValue, endAnimValue)
            valueAnimator.setValues(top)
        }

        // 3. 通过监听器更新属性值
        valueAnimator.addUpdateListener { animation ->
            val left = animation.getAnimatedValue("left")
            val top = animation.getAnimatedValue("top")
            if (left != null) {
                mMoveBlock!!.rectF!!.offsetTo((left as Float), mMoveBlock!!.rectF!!.top)
            }
            if (top != null) {
                mMoveBlock!!.rectF!!.offsetTo(mMoveBlock!!.rectF!!.left, (top as Float))
            }
            // 实时更新旋转中心 ->>关注2
            setMoveBlockRotateCenter(mMoveBlock!!, isClockWise)

            invalidate()
        }
        return valueAnimator
    }

    /**
     * 实时更新移动方块的旋转中心
     * 因为方块在平移旋转过程中，旋转中心也会跟着改变，因此需要改变MoveBlock的旋转中心（cx,cy）
     */
    private fun setMoveBlockRotateCenter(
        moveBlock: MoveBlock,
        isClockwise: Boolean
    ) {

        // 情况1：以移动方块的左上角为旋转中心
        when {
            moveBlock.index === 0 -> {
                moveBlock.cx = moveBlock.rectF!!.right
                moveBlock.cy = moveBlock.rectF!!.bottom

                // 情况2：以移动方块的右下角为旋转中心
            }
            moveBlock.index === lineNumber * lineNumber - 1 -> {
                moveBlock.cx = moveBlock.rectF!!.left
                moveBlock.cy = moveBlock.rectF!!.top

                // 情况3：以移动方块的左下角为旋转中心
            }
            moveBlock.index === lineNumber * (lineNumber - 1) -> {
                moveBlock.cx = moveBlock.rectF!!.right
                moveBlock.cy = moveBlock.rectF!!.top

                // 情况4：以移动方块的右上角为旋转中心
            }
            moveBlock.index === lineNumber - 1 -> {
                moveBlock.cx = moveBlock.rectF!!.left
                moveBlock.cy = moveBlock.rectF!!.bottom
            }
            moveBlock.index % lineNumber === 0 -> {
                moveBlock.cx = moveBlock.rectF!!.right
                moveBlock.cy = if (isClockwise) moveBlock.rectF!!.top else moveBlock.rectF!!.bottom

                // 情况2：上边
            }
            moveBlock.index < lineNumber -> {
                moveBlock.cx = if (isClockwise) moveBlock.rectF!!.right else moveBlock.rectF!!.left
                moveBlock.cy = moveBlock.rectF!!.bottom

                // 情况3：右边
            }
            (moveBlock.index + 1) % lineNumber === 0 -> {
                moveBlock.cx = moveBlock.rectF!!.left
                moveBlock.cy = if (isClockwise) moveBlock.rectF!!.bottom else moveBlock.rectF!!.top

                // 情况4：下边
            }
            moveBlock.index > (lineNumber - 1) * lineNumber -> {
                moveBlock.cx = if (isClockwise) moveBlock.rectF!!.left else moveBlock.rectF!!.right
                moveBlock.cy = moveBlock.rectF!!.top
            }
        }
    }

    /**
     * 设置旋转动画
     */
    private fun createMoveValueAnimator(): ValueAnimator {
        // 通过属性动画进行设置
        val moveAnim = ValueAnimator.ofFloat(0f, 90f).setDuration(moveSpeed.toLong())
        moveAnim.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue

            // 赋值
            mRotateDegree = animatedValue as Float
            invalidate()
        }
        return moveAnim
    }

    /**
     * 更新移动方块的位置
     */
    private fun updateMoveBlock() {
        mMoveBlock!!.rectF!!.set(mfixedBlocks[mCurrEmptyPosition]?.next!!.rectF)
        mMoveBlock!!.index = mfixedBlocks[mCurrEmptyPosition]?.next!!.index
        setMoveBlockRotateCenter(mMoveBlock!!, isClockWise)
    }

    /**
     * 停止动画
     */
    fun stopMoving() {
        // 通过标记位来设置
        mAllowRoll = false
    }


    /**
     * 判断移动方向
     * 即上下 or 左右
     */
    private fun isNextRollLeftOrRight(
        currEmptyfixedBlock: fixedBlock,
        rollSquare: fixedBlock?
    ): Boolean {
        return (currEmptyfixedBlock.rectF!!.left - rollSquare?.rectF!!.left).toInt() != 0
    }

    /**
     * 固定方块类（内部类）
     */
    private class fixedBlock {
        // 存储方块的坐标位置参数
        var rectF: RectF? = null
        // 方块对应序号
        var index = 0
        // 标志位：判断是否需要绘制
        var isShow = false
        // 指向下一个需要移动的位置
        var next: fixedBlock? = null // 外部的方块序号 ≠ 0、1、2…排列，通过 next变量（指定其下一个），一个接一个连接 外部方块 成圈
    }

    /**
     * ：移动方块类（内部类）
     */
    private class MoveBlock {
        // 存储方块的坐标位置参数
        var rectF: RectF? = null
        // 方块对应序号
        var index = 0
        // 标志位：判断是否需要绘制
        var isShow = false
        // 旋转中心坐标 移动时的旋转中心（X，Y）
        var cx = 0f
        var cy = 0f
    }


}