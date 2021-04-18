package com.nerdz.selfielib.models

import android.graphics.Bitmap
import android.media.Image

data class CameraResult(
    var image: Image,
    var modifiedImage: Bitmap? = null
)
