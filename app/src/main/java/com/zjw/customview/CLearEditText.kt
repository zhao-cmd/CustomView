package com.zjw.customview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import androidx.core.content.ContextCompat

/**
 *  author:zjw
 *  time:2021/03/17
 *  desc:继承EditText的自定义EditText
 */
class CLearEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatEditText(context, attrs, defStyleAttr) {
    private var icon: Drawable? = null

    init {
        context.theme.obtainStyledAttributes(attrs,R.styleable.CLearEditText,0,0)
            .apply {
                try {
                    val iconId=getResourceId(R.styleable.CLearEditText_clearIcon,0)
                    if (iconId!=0){
                        icon=ContextCompat.getDrawable(context,iconId)
                    }
                }finally {
                    recycle()
                }
            }
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        toggleClear()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { e ->
            icon?.let {
                if (e.action == MotionEvent.ACTION_UP
                    && e.x > width - it.intrinsicWidth
                    && e.x < width
                    && e.y > height / 2 - it.intrinsicHeight / 2
                    && e.y < height / 2 + it.intrinsicHeight / 2) {
                    text?.clear()
                }
            }
        }
        performClick()
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun toggleClear() {
        val drawable = if (text?.isNotEmpty() == true) icon else null
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
    }

}