package com.hani.assistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HaniApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any global stuff
    }
}