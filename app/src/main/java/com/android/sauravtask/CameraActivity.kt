package com.android.sauravtask

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraPreview: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var camera: Camera

    private val cameraPermission = Manifest.permission.CAMERA
    private val requestCodeCameraPermission = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraPreview = findViewById(R.id.camera_preview)
        surfaceHolder = cameraPreview.holder

        if (checkCameraPermission()) {
            initializeCamera()
        } else {
            requestCameraPermission()
        }
    }
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), requestCodeCameraPermission)
    }

    private fun initializeCamera() {
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Configure camera parameters, start preview, etc.
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                // Open the camera, configure it, and start preview
                camera = Camera.open()

                try {
                    camera.setPreviewDisplay(holder)
                    camera.startPreview()
                } catch (e: IOException) {
                    Log.e(TAG, "Error starting camera preview: ${e.message}")
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                // Release the camera when the preview surface is destroyed
                camera.stopPreview()
                camera.release()
            }
        })
    }

    private fun captureImage() {
        // Capture the image from the camera and crop it to the size of the QR rectangle viewport
        // Save the cropped image to local device storage
        // You can use camera.takePicture() and image processing libraries like OpenCV or Android's Bitmap APIs here
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestCodeCameraPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera()
            } else {
                Toast.makeText(this,"Camera permission denied",Toast.LENGTH_SHORT).show()
                // Handle the case when the user denies camera permission
            }
        }
    }

    companion object {
        private const val TAG = "CameraActivity"
    }
}