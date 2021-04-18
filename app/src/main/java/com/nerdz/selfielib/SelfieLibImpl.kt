package com.nerdz.selfielib

import com.nerdz.selfielib.screens.camera_screen.SelfieFragment

class SelfieLibImpl: SelfieLib {
    override fun startCameraAsFragment(callback: cameraResultCallback): SelfieFragment {
        return SelfieFragment()
    }
}