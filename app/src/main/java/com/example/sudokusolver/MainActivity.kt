package com.example.sudokusolver

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader
import com.example.sudokusolver.MainActivity
import android.os.Bundle
import com.example.sudokusolver.R
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.Intent
import android.util.Log
import android.view.View
import com.example.sudokusolver.SudokuBoard
import com.example.sudokusolver.ShowCameraActivity

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"

        init {
            if (!OpenCVLoader.initDebug()) {
                Log.d(TAG, "OpenCV not loaded")
            } else {
                Log.d(TAG, "OpenCV loaded")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }
    }

    fun manualInput(view: View?) {
        val intent = Intent(this, SudokuBoard::class.java)
        startActivity(intent)
    }

    fun captureInput(view: View?) {
        val intent = Intent(this, ShowCameraActivity::class.java)
        startActivity(intent)
    }
}