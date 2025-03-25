package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sin


class MainActivity : AppCompatActivity() {
    private lateinit var sensorHandler: SensorBackgroundHandler
    private lateinit var imageView: ImageView
    private lateinit var crosshairView: ImageView
    private lateinit var targetView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var targetController: TargetController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        crosshairView = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_view)
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = RelativeLayout.LayoutParams(100, 100).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            }
        }

        targetView = ImageView(this).apply {
            setImageResource(R.drawable.target)
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = RelativeLayout.LayoutParams(500, 500).apply {
                // Initially position at (0,0) to use translation
                leftMargin = 0
                topMargin = 0
            }
            elevation = 4f
            isEnabled = false
        }

        val layout = findViewById<RelativeLayout>(R.id.mainLayout)
        layout.clipChildren = false
        layout.clipToPadding = false
        layout.addView(targetView)
        layout.addView(crosshairView)

        layout.setOnClickListener {
            if (targetView.isEnabled) {
                Log.d("MainActivity", "Screen clicked, starting game")
                val intent = Intent(this@MainActivity, GameActivity::class.java)
                startActivity(intent)
            }
        }

        try {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.redneck)
            if (bitmap == null) {
                Log.e("MainActivity", "Failed to load image")
                finish()
                return
            }
            imageView.setImageBitmap(bitmap)
            targetController = TargetController(this, targetView, imageView, bitmap.width.toFloat(), bitmap.height.toFloat())
            sensorHandler = SensorBackgroundHandler(this, imageView, bitmap) { x, y ->
                targetController.updateTargetPosition(x, y)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading image: ${e.message}")
            finish()
            return
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()
        sensorHandler.resume()
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.pause()
    }
}