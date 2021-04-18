package com.nerdz.selfielib

import com.nerdz.selfielib.models.CameraResult
import com.nerdz.selfielib.screens.camera_screen.SelfieFragment

interface SelfieLib {
    fun startCameraAsFragment(callback: cameraResultCallback) : SelfieFragment
}

typealias cameraResultCallback = (CameraResult) -> Unit