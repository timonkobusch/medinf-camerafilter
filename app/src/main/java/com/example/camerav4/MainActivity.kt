package com.example.camerav4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.camerav4.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (isPermissionGranted()) {
            openCamera()
        } else {
            Log.w("myapp", "Checking Permission!")
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.CAMERA), 1
            )
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 1) {
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if ((ContextCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_LONG).show()
            }
            return
        }
    }

    private fun openCamera() {
        Log.w("myapp", "Opening Camera!")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val viewFinder = binding.viewFinder

            val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)

        }, ContextCompat.getMainExecutor(this))
    }
}