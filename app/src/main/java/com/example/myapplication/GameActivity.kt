package com.example.myapplication

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.postDelayed
import android.content.res.Configuration


class GameActivity : AppCompatActivity() {
    private lateinit var sensorHandler: SensorBackgroundHandler
    private lateinit var gunshotPlayer: MediaPlayer
    private lateinit var reloadPlayer: MediaPlayer
    private lateinit var flashView: View
    private lateinit var gunView: ImageView

    private lateinit var targetView1: ImageView
    private lateinit var targetController1: TargetController
    private var isReloading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val layout = findViewById<RelativeLayout>(R.id.gameLayout)

        // Initialize flash overlay view
        flashView = View(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.WHITE)
            alpha = 0f
            visibility = View.GONE
        }
        layout.addView(flashView)

        // Initialize gun image view
        gunView = ImageView(this).apply {
            setImageResource(R.drawable.gun3)
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            elevation = 5f
        }
        layout.addView(gunView)

        // Initialize first target
        targetView1 = ImageView(this).apply {
            setImageResource(R.drawable.target)
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = RelativeLayout.LayoutParams(500, 500).apply {
                leftMargin = 0
                topMargin = 0
            }
            elevation = 4f
            isEnabled = false
        }
        layout.addView(targetView1)


        // Load background image and initialize targets
        try {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.background)
            if (bitmap == null) {
                Log.e("GameActivity", "Failed to load background image")
                finish()
                return
            }
            imageView.setImageBitmap(bitmap)
            targetController1 = TargetController(this, targetView1, imageView, bitmap.width.toFloat(), bitmap.height.toFloat())
            sensorHandler = SensorBackgroundHandler(this, imageView, bitmap) { x, y ->
                targetController1.updateTargetPosition(x, y)
            }
        } catch (e: Exception) {
            Log.e("GameActivity", "Error loading background image: ${e.message}")
            finish()
            return
        }

        // Initialize media players
        gunshotPlayer = MediaPlayer.create(this, R.raw.gunshot)
        gunshotPlayer.setOnErrorListener { mp, what, extra ->
            Log.e("GameActivity", "Gunshot MediaPlayer error: what=$what, extra=$extra")
            true
        }

        reloadPlayer = MediaPlayer.create(this, R.raw.reload)
        reloadPlayer.setOnErrorListener { mp, what, extra ->
            Log.e("GameActivity", "Reload MediaPlayer error: what=$what, extra=$extra")
            true
        }

        // Set click listener for shooting
        layout.setOnClickListener {
            if (!isReloading) {
                fireShot()
            }
        }

        // Ensure layout doesn't clip children
        layout.clipChildren = false
        layout.clipToPadding = false
    }

    private fun fireShot() {
        if (gunshotPlayer.isPlaying) {
            gunshotPlayer.stop()
            gunshotPlayer.prepare()
        }
        gunshotPlayer.start()

        // Flash effect
        flashView.visibility = View.VISIBLE
        flashView.alpha = 0.8f
        flashView.postDelayed(10) {
            flashView.animate()
                .alpha(0f)
                .setDuration(30)
                .withEndAction { flashView.visibility = View.GONE }
                .start()
        }

        animateRecoil()
        isReloading = true
        scheduleReload()
    }

    private fun animateRecoil() {
        gunView.animate()
            .translationY(50f)
            .translationX(20f)
            .setDuration(20)
            .withEndAction {
                gunView.animate()
                    .translationY(0f)
                    .translationX(0f)
                    .setDuration(350)
                    .start()
            }
            .start()
    }

    private fun scheduleReload() {
        val reloadDelay = 700L
        flashView.postDelayed(reloadDelay) {
            playReloadSound()
            isReloading = false
        }
    }

    private fun playReloadSound() {
        if (reloadPlayer.isPlaying) {
            reloadPlayer.stop()
            reloadPlayer.prepare()
        }
        reloadPlayer.start()
    }

    override fun onResume() {
        super.onResume()
        sensorHandler.resume()
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.pause()
        if (gunshotPlayer.isPlaying) {
            gunshotPlayer.stop()
        }
        if (reloadPlayer.isPlaying) {
            reloadPlayer.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gunshotPlayer.release()
        reloadPlayer.release()
    }
}