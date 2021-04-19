package com.nerdz.selfielib.ml

import com.google.mlkit.vision.face.Face

data class Detection(
    val face: Face,
    val rotation: Int,
    val imgWidth: Int,
    val imgHeight: Int
)
