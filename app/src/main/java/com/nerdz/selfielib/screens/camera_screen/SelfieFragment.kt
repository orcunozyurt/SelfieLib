package com.nerdz.selfielib.screens.camera_screen

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieAnimationView
import com.nerdz.selfielib.R
import com.nerdz.selfielib.extensions.leftDrawable
import com.nerdz.selfielib.extensions.runOnUiThread
import com.nerdz.selfielib.ml.FaceGraphic
import com.nerdz.selfielib.ml.FrameAnalyzer
import com.nerdz.selfielib.ml.GraphicOverlay
import com.nerdz.selfielib.models.CameraResult
import com.nerdz.selfielib.views_custom.OvalOverlay
import java.util.concurrent.Executors
import kotlin.random.Random

class SelfieFragment : Fragment() {

    var listener: ((CameraResult)->Unit)? = null

    private val viewModel: SelfieViewModel by viewModels()
    private val executor = Executors.newSingleThreadExecutor()
    private val permissions = listOf(Manifest.permission.CAMERA)
    private val permissionsRequestCode = Random.nextInt(0, 10000)
    private lateinit var viewFinder: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var ovalOverlay: OvalOverlay
    private lateinit var container: ConstraintLayout
    private lateinit var textViewSmileInstruction: TextView
    private lateinit var textViewEyesInstruction: TextView
    private lateinit var textViewFacePositionInstruction: TextView
    private lateinit var landmarkButton: ImageButton
    private lateinit var animationView: LottieAnimationView

    private var showLandmarks = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_selfie, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.container)
        viewFinder = view.findViewById(R.id.view_finder)
        graphicOverlay = view.findViewById(R.id.detection_overlay)
        ovalOverlay = view.findViewById(R.id.oval_overlay)
        textViewFacePositionInstruction = view.findViewById(R.id.ovalOverlay_instruction_tw)
        textViewSmileInstruction = view.findViewById(R.id.smile_instruction_tw)
        textViewEyesInstruction = view.findViewById(R.id.eyes_instruction_tw)
        landmarkButton = view.findViewById(R.id.landmark_button)
        animationView = view.findViewById(R.id.animationView)

        landmarkButton.setOnClickListener {
            toggleLandmark()
        }

        viewModel.instructions.observe(viewLifecycleOwner, { instruction ->
            updateInstructionIcon(textViewFacePositionInstruction, instruction.isFaceOnPosition)
            updateInstructionIcon(textViewSmileInstruction, instruction.isSmiling)
            updateInstructionIcon(textViewEyesInstruction, instruction.areEyesOpen)

            if (instruction.canTakePhoto() && !animationView.isAnimating) {
                showAnimation()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        // Request permissions each time the app resumes, since they can be revoked at any time
        if (!hasPermissions(requireContext())) {
            ActivityCompat.requestPermissions(
                requireActivity(), permissions.toTypedArray(), permissionsRequestCode)
        } else {
            startCamera()
            container.postDelayed({
                container.systemUiVisibility = FLAGS_FULLSCREEN
            }, IMMERSIVE_FLAG_TIMEOUT)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /** Convenience method used to check if all permissions required by this app are granted */
    private fun hasPermissions(context: Context) = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionsRequestCode && hasPermissions(requireContext())) {
            startCamera()
        } else {
            activity?.onBackPressed() // If we don't have the required permissions, we can't run
        }
    }

    private fun startCamera() {
        viewFinder.post {
            setUpCamera()
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this.context))
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindCameraUseCases() {
        val rotation = viewFinder.display.rotation
        val lensFacing = CameraSelector.LENS_FACING_FRONT

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        // Preview
        val previewUseCase = getPreviewUseCase(rotation)
        // ImageAnalysis
        val imageAnalysisUseCase = getImageAnalysisUseCase(rotation)
        // ViewPort
        val viewport = viewFinder.viewPort

        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(previewUseCase)
            .addUseCase(imageAnalysisUseCase)
            .setViewPort(viewport!!)
            .build()

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup)

            // Attach the viewfinder's surface provider to preview use case
            previewUseCase.setSurfaceProvider(viewFinder.surfaceProvider)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPreviewUseCase(rotation: Int): Preview {
        val targetResolution = viewModel.targetResolution
        return Preview.Builder()
            .setTargetResolution(targetResolution)
            .setTargetRotation(rotation)
            .build()
    }

    private fun getImageAnalysisUseCase(rotation: Int): ImageAnalysis {
        val targetResolution = viewModel.targetResolution

        return ImageAnalysis.Builder()
            .setTargetResolution(targetResolution)
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, FrameAnalyzer { detections ->

                    val ovalArea = ovalOverlay.getOvalAreaBoundingBox()
                    if (detections.isEmpty()) {
                        graphicOverlay.clear()
                        runOnUiThread {
                            resetInstructionIcons()
                        }
                    }

                    detections.forEach { detection ->
                        graphicOverlay.setImageSourceInfo(
                            detection.imgHeight, detection.imgWidth, true)
                        val faceGraphic = FaceGraphic(graphicOverlay, detection.face, showLandmarks)
                        graphicOverlay.add(faceGraphic)

                        viewModel.processDetections(
                            ovalArea, faceGraphic.calculateScaledFace(), detection)

                    }
                })
            }
    }

    private fun updateInstructionIcon(textView: TextView, isSuccessful: Boolean) {
        val icon = if (isSuccessful) R.drawable.ic_checkmark else R.drawable.ic_warning
        val color = if (isSuccessful) R.color.colorSuccess else R.color.colorWarning
        val size = R.dimen.instruction_icon_size
        textView.leftDrawable(icon, colorRes = color, sizeRes = size)
    }

    private fun resetInstructionIcons() {
        updateInstructionIcon(textViewFacePositionInstruction, false)
        updateInstructionIcon(textViewSmileInstruction, false)
        updateInstructionIcon(textViewEyesInstruction, false)
        animationView.visibility = View.GONE
    }

    private fun showAnimation() {
        animationView.visibility = View.VISIBLE
        animationView.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                animationView.visibility = View.GONE
                listener?.invoke(CameraResult(mediaBitmap = viewFinder.bitmap))
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })
        animationView.playAnimation()
    }

    private fun toggleLandmark() {
        showLandmarks = !showLandmarks
    }


    companion object {
        const val FLAGS_FULLSCREEN =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        const val IMMERSIVE_FLAG_TIMEOUT = 500L
    }
}