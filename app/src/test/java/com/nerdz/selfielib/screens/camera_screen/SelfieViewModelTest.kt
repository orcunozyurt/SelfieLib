package com.nerdz.selfielib.screens.camera_screen

import android.graphics.RectF
import android.os.Build
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SelfieViewModelTest {

    private val viewModel: SelfieViewModel = SelfieViewModel()

    @Test
    fun isFaceOnPosition_faceOut() {
        val faceRect = RectF(0f, 5f, 50f, 80f)
        val ovalOverlayRect = RectF(0f, 10f, 150f, 100f)
        val isFaceOnPosition = viewModel.isFaceOnPosition(ovalOverlayRect, faceRect)
        Truth.assertThat(isFaceOnPosition).isFalse()
    }

    @Test
    fun isFaceOnPosition_faceIn() {
        val faceRect = RectF(0f, 20f, 50f, 80f)
        val ovalOverlayRect = RectF(0f, 10f, 150f, 100f)

        val isFaceOnPosition = viewModel.isFaceOnPosition(ovalOverlayRect, faceRect)
        Truth.assertThat(isFaceOnPosition).isTrue()
    }
}