package com.nerdz.selfielib.models

import android.media.Image

data class Instruction(
    var isFaceOnPosition: Boolean,
    var isSmiling: Boolean,
    var areEyesOpen: Boolean,
    var image : Image?
) {
    fun canTakePhoto() = isFaceOnPosition && isSmiling && areEyesOpen
}
