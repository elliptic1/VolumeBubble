package com.tbse.volumebubble

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.txusballesteros.bubbles.BubbleLayout
import com.txusballesteros.bubbles.BubblesManager
import kotlin.math.hypot
import kotlin.random.Random

class GameActivity : AppCompatActivity(), SensorEventListener {
    private val handler = Handler(Looper.getMainLooper())
    private val frameRunnable = object : Runnable {
        override fun run() {
            updatePhysics()
            handler.postDelayed(this, 16L)
        }
    }

    private lateinit var sensorManager: SensorManager
    private var bubblesManager: BubblesManager? = null

    private val bubbles = mutableListOf<PhysicsBubble>()
    private var gravityX = 0f
    private var gravityY = 9.8f
    private var screenWidth = 0
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        initWindowSize()
        initializeBubblesManager()
        createBubbles()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
        handler.post(frameRunnable)
    }

    override fun onPause() {
        handler.removeCallbacks(frameRunnable)
        sensorManager.unregisterListener(this)
        bubbles.forEach { bubblesManager?.removeBubble(it.view) }
        bubbles.clear()
        super.onPause()
    }

    private fun initWindowSize() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        wm.defaultDisplay.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
    }

    private fun initializeBubblesManager() {
        bubblesManager = BubblesManager.Builder(applicationContext)
            .setTrashLayout(R.layout.bubble_trash_layout)
            .build()
        bubblesManager?.initialize()
    }

    private fun createBubbles() {
        val random = Random(System.currentTimeMillis())
        repeat(20) {
            val bubbleView = LayoutInflater.from(this).inflate(R.layout.bubble_layout, null) as BubbleLayout
            bubbleView.setShouldStickToWall(false)
            val image = bubbleView.findViewById<ImageView>(R.id.avatar)
            image.setColorFilter(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)))
            bubbleView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val radius = bubbleView.measuredWidth / 2f
            val x = random.nextFloat() * (screenWidth - 2 * radius) + radius
            val y = random.nextFloat() * (screenHeight - 2 * radius) + radius
            bubblesManager?.addBubble(bubbleView, (x - radius).toInt(), (y - radius).toInt())
            val bubble = PhysicsBubble(bubbleView, x, y, radius)
            setupTouch(bubble)
            bubbles.add(bubble)
        }
    }

    private fun setupTouch(bubble: PhysicsBubble) {
        bubble.view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    bubble.dragging = true
                    bubble.lastTouchX = event.rawX
                    bubble.lastTouchY = event.rawY
                    bubble.lastTouchTime = System.currentTimeMillis()
                    bubble.vx = 0f
                    bubble.vy = 0f
                }
                MotionEvent.ACTION_MOVE -> {
                    val now = System.currentTimeMillis()
                    val dt = (now - bubble.lastTouchTime) / 1000f
                    if (dt > 0f) {
                        bubble.vx = (event.rawX - bubble.lastTouchX) / dt
                        bubble.vy = (event.rawY - bubble.lastTouchY) / dt
                    }
                    bubble.x = bubble.view.positionX + bubble.radius
                    bubble.y = bubble.view.positionY + bubble.radius
                    bubble.lastTouchX = event.rawX
                    bubble.lastTouchY = event.rawY
                    bubble.lastTouchTime = now
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    bubble.dragging = false
                    bubble.x = bubble.view.positionX + bubble.radius
                    bubble.y = bubble.view.positionY + bubble.radius
                }
            }
            false
        }
    }

    private fun updatePhysics() {
        val dt = 0.016f
        val gScale = 5f
        for (i in bubbles.indices) {
            val b = bubbles[i]
            if (!b.dragging) {
                b.vx += gravityX * gScale * dt
                b.vy += gravityY * gScale * dt
                b.x += b.vx * dt
                b.y += b.vy * dt
            }
            if (b.x - b.radius < 0) {
                b.x = b.radius
                b.vx = -b.vx * 0.8f
            } else if (b.x + b.radius > screenWidth) {
                b.x = screenWidth - b.radius
                b.vx = -b.vx * 0.8f
            }
            if (b.y - b.radius < 0) {
                b.y = b.radius
                b.vy = -b.vy * 0.8f
            } else if (b.y + b.radius > screenHeight) {
                b.y = screenHeight - b.radius
                b.vy = -b.vy * 0.8f
            }
        }
        for (i in 0 until bubbles.size) {
            val b1 = bubbles[i]
            for (j in i + 1 until bubbles.size) {
                val b2 = bubbles[j]
                val dx = b2.x - b1.x
                val dy = b2.y - b1.y
                val dist = hypot(dx, dy)
                val minDist = b1.radius + b2.radius
                if (dist < minDist && dist > 0f) {
                    val overlap = 0.5f * (minDist - dist)
                    val nx = dx / dist
                    val ny = dy / dist
                    b1.x -= overlap * nx
                    b1.y -= overlap * ny
                    b2.x += overlap * nx
                    b2.y += overlap * ny
                    val kx = b1.vx - b2.vx
                    val ky = b1.vy - b2.vy
                    val p = kx * nx + ky * ny
                    b1.vx -= p * nx
                    b1.vy -= p * ny
                    b2.vx += p * nx
                    b2.vy += p * ny
                }
            }
        }
        bubbles.forEach { it.view.setPosition((it.x - it.radius).toInt(), (it.y - it.radius).toInt()) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        gravityX = -event.values[0]
        gravityY = event.values[1]
    }

    private data class PhysicsBubble(
        val view: BubbleLayout,
        var x: Float,
        var y: Float,
        val radius: Float,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var dragging: Boolean = false,
        var lastTouchX: Float = 0f,
        var lastTouchY: Float = 0f,
        var lastTouchTime: Long = 0L
    )
}
