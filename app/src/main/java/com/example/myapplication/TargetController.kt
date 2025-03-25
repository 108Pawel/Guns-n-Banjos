package com.example.myapplication

import android.content.Context
import android.view.Surface
import android.widget.ImageView
import android.widget.RelativeLayout
import kotlin.math.sin

class TargetController(
    private val context: Context,
    private val targetView: ImageView,
    private val imageView: ImageView,
    private val bitmapWidth: Float,
    private val bitmapHeight: Float
) {
    private var oscillationTime = 0f
    private val targetSize = 500 // Fixed size in pixels

    init {
        targetView.scaleType = ImageView.ScaleType.CENTER
        val layoutParams = targetView.layoutParams as RelativeLayout.LayoutParams
        layoutParams.width = targetSize
        layoutParams.height = targetSize
        targetView.layoutParams = layoutParams
    }

    fun updateTargetPosition(currentX: Float, currentY: Float) {
        val targetWidth = targetSize.toFloat()
        val targetHeight = targetSize.toFloat()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        val rotation = (windowManager.defaultDisplay.rotation + 0) % 4
        val verticalOffset = when (rotation) {
            Surface.ROTATION_90 -> -20f
            Surface.ROTATION_270 -> -20f
            else -> -70f
        }

        // Calculate oscillation offset
        oscillationTime += 0.07f
        val oscillationAmplitude = 1000f
        val oscillationOffset = sin(oscillationTime) * oscillationAmplitude

        // Base position (center of bitmap)
        val baseX = (bitmapWidth - targetWidth) / 2
        val baseY = (bitmapHeight - targetHeight) / 2 + verticalOffset

        // Apply sensor input and oscillation via translation
        val targetX = baseX + currentX + oscillationOffset
        val targetY = baseY + currentY

        // Use translation instead of margins
        targetView.translationX = targetX
        targetView.translationY = targetY

        checkTargetEnabled()
    }

    private fun checkTargetEnabled() {
        val screenCenterX = imageView.width / 2
        val screenCenterY = imageView.height / 2

        val targetLeft = targetView.left + targetView.translationX
        val targetRight = targetLeft + targetView.width
        val targetTop = targetView.top + targetView.translationY
        val targetBottom = targetTop + targetView.height

        val isCrosshairOverTarget = screenCenterX >= targetLeft &&
                screenCenterX <= targetRight &&
                screenCenterY >= targetTop &&
                screenCenterY <= targetBottom

        targetView.isEnabled = isCrosshairOverTarget
        targetView.alpha = if (isCrosshairOverTarget) 1.0f else 0.8f
    }
}