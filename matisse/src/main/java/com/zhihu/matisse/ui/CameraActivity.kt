package com.zhihu.matisse.ui

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.zhihu.matisse.R
import com.zhihu.matisse.databinding.ActivityCameraBinding
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.configuration.UpdateConfiguration
import io.fotoapparat.log.none
import io.fotoapparat.parameter.Flash
import io.fotoapparat.parameter.Zoom
import io.fotoapparat.selector.LensPositionSelector
import io.fotoapparat.selector.autoFocus
import io.fotoapparat.selector.back
import io.fotoapparat.selector.continuousFocusPicture
import io.fotoapparat.selector.firstAvailable
import io.fotoapparat.selector.fixed
import io.fotoapparat.selector.front
import io.fotoapparat.selector.highestFps
import io.fotoapparat.selector.highestResolution
import io.fotoapparat.selector.off
import io.fotoapparat.selector.standardRatio
import io.fotoapparat.selector.torch
import io.fotoapparat.selector.wideRatio
import java.io.File
import kotlin.math.roundToInt

class CameraActivity : AppCompatActivity() {

    private val permissionsDelegate = PermissionsDelegate(this)

    private var permissionsGranted: Boolean = false
    private var activeCamera: Camera = Camera.Back

    private lateinit var fotoapparat: Fotoapparat
    private lateinit var cameraZoom: Zoom.VariableZoom
    private lateinit var binding: ActivityCameraBinding

    private var curZoom: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        binding.lifecycleOwner = this

        permissionsGranted = permissionsDelegate.hasCameraPermission()

        if (permissionsGranted) {
            binding.cameraView.visibility = View.VISIBLE
        } else {
            permissionsDelegate.requestCameraPermission()
        }

        fotoapparat = Fotoapparat(
            context = this,
            view = binding.cameraView,
            focusView = binding.focusView,
//            logger = logcat(),
            logger = none(),
            lensPosition = activeCamera.lensPosition,
            cameraConfiguration = activeCamera.configuration,
            cameraErrorCallback = { Log.e(LOGGING_TAG, "Camera error: ", it) }
        )

        binding.capture onClick takePicture()
        binding.switchCamera onClick changeCamera()
        binding.torchSwitch onCheckedChanged toggleFlash()
    }

    private fun takePicture(): () -> Unit = {
        val photoResult = fotoapparat
            .autoFocus()
            .takePicture()
        val output = intent.extras?.getString(MediaStore.EXTRA_OUTPUT)
        val file = if (output != null) {
            File(output)
        } else {
            File(getExternalFilesDir("photos"), "photo.jpg")
        }
        photoResult.saveToFile(file).whenAvailable {
            setResult(RESULT_OK)
            finish()
        }

//        photoResult
//            .toBitmap(scaled(scaleFactor = 0.25f))
//            .whenAvailable { photo ->
//                photo
//                    ?.let {
//                        Log.i(
//                            LOGGING_TAG,
//                            "New photo captured. Bitmap length: ${it.bitmap.byteCount}"
//                        )
//                        val bm = it.bitmap
//
//
//                        imageView.setImageBitmap(it.bitmap)
//                        imageView.rotation = (-it.rotationDegrees).toFloat()
//                    }
//                    ?: Log.e(LOGGING_TAG, "Couldn't capture photo.")
//            }
    }

    private fun changeCamera(): () -> Unit = {
        activeCamera = when (activeCamera) {
            Camera.Front -> Camera.Back
            Camera.Back -> Camera.Front
        }

        fotoapparat.switchTo(
            lensPosition = activeCamera.lensPosition,
            cameraConfiguration = activeCamera.configuration
        )

        adjustViewsVisibility()

        binding.torchSwitch.isChecked = false

        Log.i(
            LOGGING_TAG,
            "New camera position: ${if (activeCamera is Camera.Back) "back" else "front"}"
        )
    }

    private fun toggleFlash(): (CompoundButton, Boolean) -> Unit = { _, isChecked ->
        fotoapparat.updateConfiguration(
            UpdateConfiguration(
                flashMode = if (isChecked) {
                    firstAvailable(
                        torch(),
                        off()
                    )
                } else {
                    off()
                }
            )
        )

        Log.i(LOGGING_TAG, "Flash is now ${if (isChecked) "on" else "off"}")
    }

    override fun onStart() {
        super.onStart()
        if (permissionsGranted) {
            fotoapparat.start()
            adjustViewsVisibility()
        }
    }

    override fun onStop() {
        super.onStop()
        if (permissionsGranted) {
            fotoapparat.stop()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            permissionsGranted = true
            fotoapparat.start()
            adjustViewsVisibility()
            binding.cameraView.visibility = View.VISIBLE
        }
    }

    private fun adjustViewsVisibility() {
        fotoapparat.getCapabilities()
            .whenAvailable { capabilities ->
                capabilities
                    ?.let {
//                        val focusView = binding.focusView
                        (it.zoom as? Zoom.VariableZoom)
                            ?.let {
                                cameraZoom = it

                                /*
                                @Deprecated("")
                                focusView.scaleListener = this::scaleZoom
                                focusView.ptrListener = this::pointerChanged
                                */
                            }
                            ?: run {
                                binding.zoomLvl.visibility = View.GONE
                                /*
                                 @Deprecated("")
                                focusView.scaleListener = null
                                focusView.ptrListener = null
                                */
                            }

                        binding.torchSwitch.visibility =
                            if (it.flashModes.contains(Flash.Torch)) View.VISIBLE else View.GONE
                    }
                    ?: Log.e(LOGGING_TAG, "Couldn't obtain capabilities.")
            }

        binding.switchCamera.visibility =
            if (fotoapparat.isAvailable(front())) View.VISIBLE else View.GONE
    }

    //When zooming slowly, the values are approximately 0.9 ~ 1.1
    private fun scaleZoom(scaleFactor: Float) {
        //convert to -0.1 ~ 0.1
        val plusZoom = if (scaleFactor < 1) -1 * (1 - scaleFactor) else scaleFactor - 1
        val newZoom = curZoom + plusZoom
        if (newZoom < 0 || newZoom > 1) return

        curZoom = newZoom
        fotoapparat.setZoom(curZoom)
        val progress = (cameraZoom.maxZoom * curZoom).roundToInt()
        val value = cameraZoom.zoomRatios[progress]
        val roundedValue = ((value.toFloat()) / 10).roundToInt().toFloat() / 10

        binding.zoomLvl.visibility = View.VISIBLE
        binding.zoomLvl.text = String.format("%.1f×", roundedValue)
    }

    private fun pointerChanged(fingerCount: Int) {
        if (fingerCount == 0) {
            binding.zoomLvl.visibility = View.GONE
        }
    }
}

private const val LOGGING_TAG = "Fotoapparat Example"

private sealed class Camera(
    val lensPosition: LensPositionSelector,
    val configuration: CameraConfiguration
) {

    data object Back : Camera(
        lensPosition = back(),
        configuration = CameraConfiguration(
            previewResolution = firstAvailable(
                wideRatio(highestResolution()),
                standardRatio(highestResolution())
            ),
            previewFpsRange = highestFps(),
            flashMode = off(),
            focusMode = firstAvailable(
                continuousFocusPicture(),
                autoFocus()
            ),
            frameProcessor = {
                // Do something with the preview frame
            }
        )
    )

    data object Front : Camera(
        lensPosition = front(),
        configuration = CameraConfiguration(
            previewResolution = firstAvailable(
                wideRatio(highestResolution()),
                standardRatio(highestResolution())
            ),
            previewFpsRange = highestFps(),
            flashMode = off(),
            focusMode = firstAvailable(
                fixed(),
                autoFocus()
            )
        )
    )
}
