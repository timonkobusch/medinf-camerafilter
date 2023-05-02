package com.example.camerav4

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.camerav4.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity()  {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private var filterToggle = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.filterButton.setOnClickListener {
            toggleFilter()
        }


        if (isPermissionGranted()) {
            openCamera()
        } else {
            Log.w("myapp", "Checking Permission!")
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.CAMERA), 1
            )
        }
    }
    private fun toggleFilter() {
        filterToggle = !filterToggle

        val matrix = ColorMatrix()
        matrix.setSaturation(0F)

        if (filterToggle) {
            binding.imageView.colorFilter = ColorMatrixColorFilter(matrix)
        } else {
            binding.imageView.colorFilter = null
        }
        Log.d("myapp", "Toggling filter to: $filterToggle")
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

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalyzer { bitmap ->
                        runOnUiThread{
                            binding.imageView.setImageBitmap(bitmap)
                        }
                    })
                }

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e("myapp", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    private class ImageAnalyzer(private val listener: (Bitmap) -> Unit) : ImageAnalysis.Analyzer {

        override fun analyze(image: ImageProxy) {

            val currentBitmap = image.toBitmap()
            val rotatedBitmap = currentBitmap.rotate(90f)

            listener(rotatedBitmap)
            image.close()
        }
        fun Bitmap.rotate(fl: Float): Bitmap {

            val matrix = Matrix().apply { postRotate(fl) }
            return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)

        }
    }

}
