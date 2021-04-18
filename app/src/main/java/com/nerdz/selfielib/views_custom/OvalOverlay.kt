package com.nerdz.selfielib.views_custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt


class OvalOverlay : View {
    enum class Style {
        RECT, QUAD
    }

    private var scale = 1.0f
    private val rect = RectF()
    private val path = Path()
    private var offsetX = 0
    private var offsetY = 0
    private var mBackgroundColor = 0
    private var minPaddingLeft = 0
    private var minPaddingTop = 0
    private var minPaddingRight = 0
    private var minPaddingBottom = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mBackgroundColor = Color.parseColor("#99222222")
        setMinPadding(dp2px(24f), dp2px(80f), dp2px(24f), dp2px(80f))
    }

    fun getOvalAreaBoundingBox() : RectF {
        return rect
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        update()
    }

    private fun update() {
        val maxWidth = width - minPaddingLeft - minPaddingRight
        val maxHeight = height - minPaddingTop - minPaddingBottom
        var rectWidth: Int
        var rectHeight: Int
        rectWidth = width - minPaddingLeft - minPaddingRight
        rectHeight = Math.round(rectWidth * 1.2f)

        if (rectHeight > maxHeight) {
            val scaleFactor = maxHeight.toFloat() / rectHeight.toFloat()
            rectWidth = Math.round(rectWidth.toFloat() * scaleFactor)
            rectHeight = Math.round(rectHeight.toFloat() * scaleFactor)
        }
        if (rectWidth > maxWidth) {
            val scaleFactor = maxWidth.toFloat() / rectWidth.toFloat()
            rectWidth = Math.round(rectWidth.toFloat() * scaleFactor)
            rectHeight = Math.round(rectHeight.toFloat() * scaleFactor)
        }
        rect.left = width / 2.0f - rectWidth / 2.0f
        rect.right = width / 2.0f + rectWidth / 2.0f
        rect.top = height / 2.0f - rectHeight / 2.0f
        rect.bottom = height / 2.0f + rectHeight / 2.0f
        val rw = rect.width()
        val rh = rect.height()
        rect.left = offsetX + width / 2.0f - Math.round(rw / 2.0f * scale)
        rect.right = offsetX + width / 2.0f + Math.round(rw / 2.0f * scale)
        rect.top = offsetY + height / 2.0f - Math.round(rh / 2.0f * scale)
        rect.bottom = offsetY + height / 2.0f + Math.round(rh / 2.0f * scale)

        path.reset()
        path.addOval(
            rect,
            Path.Direction.CCW
        )
    }

    private fun setMinPadding(left: Int, top: Int, right: Int, bottom: Int) {
        minPaddingLeft = left
        minPaddingTop = top
        minPaddingRight = right
        minPaddingBottom = bottom
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(path, Region.Op.DIFFERENCE)
        canvas.drawColor(mBackgroundColor)
        canvas.restore()
    }

    private fun dp2px(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density).roundToInt()
    }
}