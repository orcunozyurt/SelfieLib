package com.nerdz.selfielib.ml

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

typealias DetectionsResultListener = (detectionList: List<Detection>) -> Unit

class FrameAnalyzer(val detectionsResultListener: DetectionsResultListener): ImageAnalysis.Analyzer {
    // High-accuracy landmark detection and face classification
    val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

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
                        mediaImage.height,
                        mediaImage
                    )
                detections.add(detection)
                //bitmap = overlayBitmaps(bitmap, blurBitmap, bbx.left.toFloat(), bbx.top.toFloat())
            }
            detectionsResultListener(detections)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        image.close()
    }
}