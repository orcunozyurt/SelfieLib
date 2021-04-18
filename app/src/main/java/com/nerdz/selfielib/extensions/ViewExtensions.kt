package com.nerdz.selfielib.extensions

import android.graphics.PorterDuff
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun TextView.leftDrawable(@DrawableRes id: Int = 0, @DimenRes sizeRes: Int = 0, @ColorInt color: Int = 0, @ColorRes colorRes: Int = 0) {
    val drawable = ContextCompat.getDrawable(context, id)
    if (sizeRes != 0) {
        val size = resources.getDimensionPixelSize(sizeRes)
        drawable?.setBounds(0, 0, size, size)
    }
    if (color != 0) {
        drawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    } else if (colorRes != 0) {
        val colorInt = ContextCompat.getColor(context, colorRes)
        drawable?.setColorFilter(colorInt, PorterDuff.Mode.SRC_ATOP)
    }
    this.setCompoundDrawables(drawable, null, drawable, null)
}