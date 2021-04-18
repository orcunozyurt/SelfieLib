package com.nerdz.selfielib.screens.camera_screen

import android.graphics.RectF
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nerdz.selfielib.ml.Detection
import com.nerdz.selfielib.models.Instruction

class CameraViewModel : ViewModel(){
    val targetResolution = Size(480, 640)

    private var _instructions: MutableLiveData<Instruction> =
        MutableLiveData(
            Instruction(
                isFaceOnPosition = false,
                isSmiling = false,
                areEyesOpen = false,
                image = null
            )
        )
    var instructions: LiveData<Instruction> = _instructions


    fun processDetections(ovalAreaBoundingBox: RectF, scaledFaceBoundingBox: RectF, detection: Detection) {
        val isFaceOnPosition = isFaceOnPosition(ovalAreaBoundingBox, scaledFaceBoundingBox)
        val face = detection.face

        val smilingProbability = face.smilingProbability ?: 0f
        val isSmiling = smilingProbability >= threshold

        val leftEyeOpenProbability = face.leftEyeOpenProbability ?: 0f
        val rightEyeOpenProbability = face.rightEyeOpenProbability ?: 0f
        val areEyesOpen =
            leftEyeOpenProbability >= threshold && rightEyeOpenProbability >= threshold

        val instruction = Instruction(
            isFaceOnPosition = isFaceOnPosition,
            isSmiling = isSmiling,
            areEyesOpen = areEyesOpen,
            image = detection.image
        )
        _instructions.postValue(instruction)
    }

    private fun isFaceOnPosition(ovalAreaBoundingBox: RectF, scaledFaceBoundingBox: RectF) : Boolean {
        return ovalAreaBoundingBox.contains(scaledFaceBoundingBox)
    }

    companion object {
        const val threshold = 0.6f
    }
}