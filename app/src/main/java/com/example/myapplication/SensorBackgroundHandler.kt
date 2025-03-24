package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Surface
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SensorBackgroundHandler(
    private val context: Context,
    private val imageView: ImageView,
    private val bitmap: Bitmap,
    private val onPositionUpdated: ((Float, Float) -> Unit)? = null // Optional callback
) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val matrix = Matrix()
    private var currentX = 0f
    private var currentY = 0f
    private var isSensorReady = false
    private var lastRotation = -1

    init {
        setupImageView()
        initializeSensor()
    }

    private fun initializeSensor() {
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (gyroscope == null) {
            Log.e("SensorBackgroundHandler", "Gyroscope sensor not available")
            (context as? AppCompatActivity)?.finish()
            return
        }
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI)
        isSensorReady = true
    }

    private fun setupImageView() {
        imageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                updateLayoutForOrientation()
            }
        })
    }

    private fun updateLayoutForOrientation() {
        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()
        val viewWidth = imageView.width.toFloat()
        val viewHeight = imageView.height.toFloat()

        currentX = -(bitmapWidth - viewWidth) / 2
        currentY = -(bitmapHeight - viewHeight) / 2

        currentX = currentX.coerceIn(-(bitmapWidth - viewWidth), 0f)
        currentY = currentY.coerceIn(-(bitmapHeight - viewHeight), 0f)

        matrix.reset()
        matrix.setTranslate(currentX, currentY)
        imageView.imageMatrix = matrix
        onPositionUpdated?.invoke(currentX, currentY) // Notify position change
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isSensorReady || event?.sensor?.type != Sensor.TYPE_GYROSCOPE) return

        val rotation = ((context as AppCompatActivity).windowManager.defaultDisplay.rotation + 0) % 4
        if (lastRotation != rotation) {
            lastRotation = rotation
            updateLayoutForOrientation()
        }

        var pitch = event.values[0]
        var roll = event.values[1]

        when (rotation) {
            Surface.ROTATION_0 -> {
                pitch = pitch
                roll = roll
            }
            Surface.ROTATION_90 -> {
                val temp = pitch
                pitch = -roll
                roll = temp
            }
            Surface.ROTATION_270 -> {
                val temp = pitch
                pitch = roll
                roll = -temp
            }
            Surface.ROTATION_180 -> {
                pitch = pitch
                roll = -roll
            }
        }

        val deltaX = roll * 80f
        val deltaY = pitch * 80f

        currentX += deltaX
        currentY += deltaY

        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()
        val viewWidth = imageView.width.toFloat()
        val viewHeight = imageView.height.toFloat()

        currentX = currentX.coerceIn(-(bitmapWidth - viewWidth), 0f)
        currentY = currentY.coerceIn(-(bitmapHeight - viewHeight), 0f)

        matrix.reset()
        matrix.setTranslate(currentX, currentY)
        imageView.imageMatrix = matrix
        onPositionUpdated?.invoke(currentX, currentY) // Notify position change
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun resume() {
        if (isSensorReady) {
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun pause() {
        if (isSensorReady) {
            sensorManager.unregisterListener(this)
        }
    }

    fun getCurrentX(): Float = currentX
    fun getCurrentY(): Float = currentY
}