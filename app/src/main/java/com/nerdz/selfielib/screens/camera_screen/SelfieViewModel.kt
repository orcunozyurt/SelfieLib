package com.nerdz.selfielib.screens.camera_screen

import android.graphics.RectF
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nerdz.selfielib.ml.Detection
import com.nerdz.selfielib.models.Instruction

class SelfieViewModel : ViewModel(){
    // Camera Resolution
    val targetResolution = Size(1080, 1920)

    private var _instructions: MutableLiveData<Instruction> =
        MutableLiveData(
            Instruction(
                isFaceOnPosition = false,
                isSmiling = false,
                areEyesOpen = false
            )
        )
    // instructions should be observed to adapt UI to instructions
    var instructions: LiveData<Instruction> = _instructions

    /**
     * processDetections processes the detections and notifies the UI for
     * isFaceOnPosition, isSmiling, areEyesOpen
     */
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
            areEyesOpen = areEyesOpen
        )
        _instructions.postValue(instruction)
    }

    /**
     * isFaceOnPosition calculates if the face detected is in desired area
     * @param ovalAreaBoundingBox is the desired box Rectangle
     * @param scaledFaceBoundingBox is the bounding box of the face
     */
    fun isFaceOnPosition(ovalAreaBoundingBox: RectF, scaledFaceBoundingBox: RectF) : Boolean {
        return ovalAreaBoundingBox.contains(scaledFaceBoundingBox)
    }


    companion object {
        // meaningful probability threshold
        const val threshold = 0.6f
    }
}