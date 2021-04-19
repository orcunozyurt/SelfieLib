package com.nerdz.selfielib.ml

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View
import androidx.core.util.Preconditions

class GraphicOverlay : View {
    private val lock = Any()
    private val graphics: MutableList<Graphic> = mutableListOf()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var scaleFactor = 1.0f
    private var rotation: Int = 0
    private var postScaleWidthOffset = 0f
    private var postScaleHeightOffset = 0f
    private var isImageFlipped = true
    private var needUpdateTransformation = true

    // Matrix for transforming from image coordinates to overlay view coordinates.
    private val transformationMatrix: Matrix = Matrix()

    constructor(
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        addOnLayoutChangeListener { view: View?, left: Int, top: Int, right: Int, bottom: Int,
                                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            needUpdateTransformation = true
        }
    }

    /** Removes all graphics from the overlay.  */
    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }

    /** Adds a graphic to the overlay.  */
    fun add(graphic: Graphic) {
        clear()
        synchronized(lock) { graphics.add(graphic) }
    }

    /** Removes a graphic from the overlay.  */
    fun remove(graphic: Graphic) {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }

    @SuppressLint("RestrictedApi")
    fun setImageSourceInfo(imageWidth: Int, imageHeight: Int, isFlipped: Boolean) {
        Preconditions.checkState(imageWidth > 0, "image width must be positive")
        Preconditions.checkState(imageHeight > 0, "image height must be positive")
        synchronized(lock) {
            this.imageWidth = imageWidth
            this.imageHeight = imageHeight
            isImageFlipped = isFlipped
            needUpdateTransformation = true
        }
        postInvalidate()
    }

    private fun updateTransformationIfNeeded() {
        if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
            return
        }
        val viewAspectRatio = width.toFloat() / height
        val imageAspectRatio: Float = imageWidth.toFloat() / imageHeight
        postScaleWidthOffset = 0f
        postScaleHeightOffset = 0f
        if (viewAspectRatio > imageAspectRatio) {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = width.toFloat() / imageWidth
            postScaleHeightOffset = (width.toFloat() / imageAspectRatio - height) / 2
        } else {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = height.toFloat() / imageHeight
            postScaleWidthOffset = (height.toFloat() * imageAspectRatio - width) / 2
        }
        transformationMatrix.reset()
        transformationMatrix.setScale(scaleFactor, scaleFactor)
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset)
        if (isImageFlipped) {
            transformationMatrix.postScale(-1f, 1f, width / 2f, height / 2f)
        }
        needUpdateTransformation = false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        synchronized(lock) {
            updateTransformationIfNeeded()
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }

    abstract class Graphic {
        private var overlay: GraphicOverlay? = null

        constructor(overlay: GraphicOverlay?) {
            this.overlay = overlay
        }

        abstract fun draw(canvas: Canvas?)

        /** Adjusts the supplied value from the image scale to the view scale.  */
        open fun scale(imagePixel: Float): Float {
            return imagePixel * overlay!!.scaleFactor
        }

        /**
         * Adjusts the x coordinate from the image's coordinate system to the view coordinate system.
         */
        open fun translateX(x: Float): Float {
            return if (overlay!!.isImageFlipped) {
                overlay!!.width - (scale(x) - overlay!!.postScaleWidthOffset)
            } else {
                scale(x) - overlay!!.postScaleWidthOffset
            }
        }

        /**
         * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
         */
        open fun translateY(y: Float): Float {
            return scale(y) - overlay!!.postScaleHeightOffset
        }

        /**
         * Returns a [Matrix] for transforming from image coordinates to overlay view coordinates.
         */
        open fun getTransformationMatrix(): Matrix? {
            return overlay!!.transformationMatrix
        }

        open fun postInvalidate() {
            overlay!!.postInvalidate()
        }
    }

}