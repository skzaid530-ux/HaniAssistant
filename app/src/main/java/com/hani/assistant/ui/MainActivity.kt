package com.hani.assistant.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hani.assistant.R
import com.hani.assistant.services.overlay.FloatingOverlayService
import com.hani.assistant.ui.settings.SettingsActivity
import com.hani.assistant.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var permissionHelper: PermissionHelper

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            startFloatingService()
        } else {
            Toast.makeText(this, "Overlay permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check all permissions
        if (permissionHelper.hasAllPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            } else {
                startFloatingService()
            }
        } else {
            permissionHelper.requestAllPermissions(this) { granted ->
                if (granted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                        requestOverlayPermission()
                    } else {
                        startFloatingService()
                    }
                } else {
                    Toast.makeText(this, "Permissions needed", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // Button to open settings
        findViewById<android.widget.Button>(R.id.btn_settings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Button to manually wake
        findViewById<android.widget.Button>(R.id.btn_wake)?.setOnClickListener {
            // Send broadcast or call orchestration
            // For simplicity, we'll just send an intent
            val intent = Intent("WAKE_UP_HANI")
            sendBroadcast(intent)
        }

        // Button to sleep
        findViewById<android.widget.Button>(R.id.btn_sleep)?.setOnClickListener {
            val intent = Intent("SLEEP_HANI")
            sendBroadcast(intent)
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        finish()
    }
}
