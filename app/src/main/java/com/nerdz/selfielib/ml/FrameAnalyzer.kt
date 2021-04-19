package com.nerdz.selfielib.ml

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

typealias DetectionsResultListener = (detectionList: List<Detection>) -> Unit

/**
 * ImageAnalysis class. Responsible from analysing each and every frame that is sent by CameraX API.
 * CameraX API is a wrapper of Camera2.
 * @param detectionsResultListener:  All face detections are passed back by this listener.
 */
class FrameAnalyzer(val detectionsResultListener: DetectionsResultListener)
    : ImageAnalysis.Analyzer {

    // Real-time contour detection
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        val rotationDegrees = image.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        try {
            val detections = mutableListOf<Detection>()
            val faces = Tasks.await(detector.process(inputImage))
            for (face in faces) {
                val detection =
                    Detection(
                        face,
                        rotationDegrees,
                        mediaImage!!.width,
                        mediaImage.height
                    )
                detections.add(detection)
            }
            detectionsResultListener(detections)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        image.close()
    }
}