package com.hani.assistant.services.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.hani.assistant.R
import com.hani.assistant.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var imageView: ImageView
    private lateinit var statusText: TextView

    private var params: WindowManager.LayoutParams? = null

    private var isDragging = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    // Animation states
    private var currentState = State.IDLE

    enum class State {
        IDLE, LISTENING, THINKING, TALKING, SLEEPING, ERROR
    }

    private val animationHandler = Handler(Looper.getMainLooper())
    private var stateUpdateJob: Job? = null

    @Inject
    lateinit var overlayViewModel: OverlayStateManager

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflate overlay layout
        overlayView = LayoutInflater.from(this).inflate(R.layout.layout_overlay, null)
        imageView = overlayView.findViewById(R.id.overlay_image)
        statusText = overlayView.findViewById(R.id.overlay_status)

        // Setup window params
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        windowManager.addView(overlayView, params)

        // Set touch listener for dragging and tap
        overlayView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    initialX = params!!.x
                    initialY = params!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isDragging = true
                        params!!.x = initialX + dx.toInt()
                        params!!.y = initialY + dy.toInt()
                        windowManager.updateViewLayout(overlayView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Tap: toggle listening or trigger command? We'll use it to wake.
                        // For now, we can open settings or toggle.
                    }
                    true
                }
                else -> false
            }
        }

        // Set long press to open settings
        overlayView.setOnLongClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        }

        // Start foreground
        startForeground(1, createNotification())

        // Observe view model state changes
        lifecycleScope.launch {
            overlayViewModel.state.collect { state ->
                setState(state)
            }
        }

        // Start with IDLE
        setState(State.IDLE)
    }

    private fun createNotification(): Notification {
        val channelId = "hani_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hani Assistant",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Hani is running")
            .setContentText("Tap to open")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun setState(state: State) {
        currentState = state
        val text = when (state) {
            State.IDLE -> "Idle"
            State.LISTENING -> "Listening..."
            State.THINKING -> "Thinking..."
            State.TALKING -> "Talking..."
            State.SLEEPING -> "Sleeping"
            State.ERROR -> "Error"
        }
        statusText.text = text
        // Trigger animations
        when (state) {
            State.IDLE -> startIdleAnimation()
            State.LISTENING -> startListeningAnimation()
            State.THINKING -> startThinkingAnimation()
            State.TALKING -> startTalkingAnimation()
            State.SLEEPING -> startSleepingAnimation()
            State.ERROR -> startErrorAnimation()
        }
    }

    private fun startIdleAnimation() {
        // Breathing animation
        val breathe = AnimationUtils.loadAnimation(this, R.anim.breathe)
        imageView.startAnimation(breathe)
    }

    private fun startListeningAnimation() {
        // Pulsing glow
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        imageView.startAnimation(pulse)
    }

    private fun startThinkingAnimation() {
        // Rotate
        val rotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        imageView.startAnimation(rotate)
    }

    private fun startTalkingAnimation() {
        // Shake / bounce
        val talk = AnimationUtils.loadAnimation(this, R.anim.talk)
        imageView.startAnimation(talk)
    }

    private fun startSleepingAnimation() {
        // Fade out and scale down
        val sleep = AnimationUtils.loadAnimation(this, R.anim.sleep)
        imageView.startAnimation(sleep)
    }

    private fun startErrorAnimation() {
        // Shake
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        imageView.startAnimation(shake)
    }

    override fun onDestroy() {
        super.onDestroy()
        stateUpdateJob?.cancel()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // LifecycleScope extension
    private val lifecycleScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
}