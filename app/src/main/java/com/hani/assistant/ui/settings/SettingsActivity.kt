package com.hani.assistant.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hani.assistant.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // For simplicity, add fragments or preferences
    }
}
