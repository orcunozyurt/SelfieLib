package com.nerdz.selfielib.models

import android.graphics.Bitmap

data class Instruction(
    var isFaceOnPosition: Boolean,
    var isSmiling: Boolean,
    var areEyesOpen: Boolean
) {
    fun canTakePhoto() = isFaceOnPosition && isSmiling && areEyesOpen
}
