package com.hani.assistant.services.wake

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeWordEngine @Inject constructor(
    private val context: Context
) {

    private val _isWakeWordDetected = MutableStateFlow(false)
    val isWakeWordDetected: StateFlow<Boolean> = _isWakeWordDetected

    private var listening = false

    fun startListening(onWakeWord: () -> Unit) {
        listening = true
        // Wake word disabled temporarily
    }

    fun stopListening() {
        listening = false
    }

    fun isListening(): Boolean = listening
}
