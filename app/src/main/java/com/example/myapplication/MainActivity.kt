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


class MainActivity : AppCompatActivity() {
    private lateinit var sensorHandler: SensorBackgroundHandler
    private lateinit var imageView: ImageView
    private lateinit var crosshairView: ImageView
    private lateinit var startGameButton: Button
    private lateinit var bitmap: Bitmap

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

        startGameButton = Button(this).apply {
            text = "Start Game"
            textSize = 28f
            setBackgroundColor(android.graphics.Color.parseColor("#00BFFF"))
            elevation = 4f
            isEnabled = false
        }

        val layout = findViewById<RelativeLayout>(R.id.mainLayout)
        val buttonParams = RelativeLayout.LayoutParams(400, 160)
        layout.addView(startGameButton, buttonParams)
        layout.addView(crosshairView)

        layout.setOnClickListener {
            if (startGameButton.isEnabled) {
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
            sensorHandler = SensorBackgroundHandler(this, imageView, bitmap) { x, y ->
                updateButtonPosition(x, y) // Update button position on every movement
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading image: ${e.message}")
            finish()
            return
        }
    }

    private fun updateButtonPosition(currentX: Float, currentY: Float) {
        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()
        val buttonWidth = startGameButton.width.toFloat()
        val buttonHeight = startGameButton.height.toFloat()

        val rotation = (windowManager.defaultDisplay.rotation + 0) % 4
        val verticalOffset = when (rotation) {
            Surface.ROTATION_90 -> -20f  // Landscape left
            Surface.ROTATION_270 -> -20f // Landscape right
            else -> -70f                 // Portrait or upside down
        }

        val buttonX = currentX + (bitmapWidth - buttonWidth) / 2
        val buttonY = currentY + (bitmapHeight - buttonHeight) / 2 + verticalOffset

        val layoutParams = startGameButton.layoutParams as RelativeLayout.LayoutParams
        layoutParams.leftMargin = buttonX.toInt()
        layoutParams.topMargin = buttonY.toInt()
        startGameButton.layoutParams = layoutParams

        checkButtonEnabled()
    }

    private fun checkButtonEnabled() {
        val screenCenterX = imageView.width / 2
        val screenCenterY = imageView.height / 2

        val buttonLeft = startGameButton.left
        val buttonRight = startGameButton.right
        val buttonTop = startGameButton.top
        val buttonBottom = startGameButton.bottom

        val isCrosshairOverButton = screenCenterX >= buttonLeft &&
                screenCenterX <= buttonRight &&
                screenCenterY >= buttonTop &&
                screenCenterY <= buttonBottom

        startGameButton.isEnabled = isCrosshairOverButton
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // The sensor handler will handle orientation changes, callback will update button
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