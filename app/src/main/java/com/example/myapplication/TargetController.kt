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
    private val targetSize = 500 // Fixed size in pixels, matching MainActivity

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

        // Apply sensor input with oscillation
        val targetX = currentX + (bitmapWidth - targetWidth) / 2 + oscillationOffset
        val targetY = currentY + (bitmapHeight - targetHeight) / 2 + verticalOffset

        val layoutParams = targetView.layoutParams as RelativeLayout.LayoutParams
        layoutParams.leftMargin = targetX.toInt()
        layoutParams.topMargin = targetY.toInt()
        targetView.layoutParams = layoutParams

        checkTargetEnabled()
    }

    private fun checkTargetEnabled() {
        val screenCenterX = imageView.width / 2
        val screenCenterY = imageView.height / 2

        val targetLeft = targetView.left
        val targetRight = targetView.right
        val targetTop = targetView.top
        val targetBottom = targetView.bottom

        val isCrosshairOverTarget = screenCenterX >= targetLeft &&
                screenCenterX <= targetRight &&
                screenCenterY >= targetTop &&
                screenCenterY <= targetBottom

        targetView.isEnabled = isCrosshairOverTarget
        targetView.alpha = if (isCrosshairOverTarget) 1.0f else 0.8f
    }
}