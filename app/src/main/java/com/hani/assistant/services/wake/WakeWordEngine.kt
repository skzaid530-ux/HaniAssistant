package com.hani.assistant.services.wake

import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeWordEngine @Inject constructor(
    private val context: Context
) {
    private val _isWakeWordDetected = MutableStateFlow(false)
    val isWakeWordDetected: StateFlow<Boolean> = _isWakeWordDetected

    private var porcupineManager: PorcupineManager? = null
    private var isListening = false

    // Replace with your custom keyword path and model
    // For production, you generate a .ppn file for "Hani" from Picovoice Console
    // For demo, we use the built-in keyword "porcupine"
    private val keywordPath = "porcupine" // or path to your .ppn file
    private val modelPath = Porcupine.BUILT_IN_MODEL_PATH

    fun startListening(onWakeWord: () -> Unit) {
        if (isListening) return
        try {
            porcupineManager = PorcupineManager(
                context,
                keywordPath,
                object : PorcupineManager.WakeWordCallback {
                    override fun invoke(keywordIndex: Int) {
                        Log.d("WakeWord", "Wake word detected")
                        _isWakeWordDetected.value = true
                        CoroutineScope(Dispatchers.Main).launch {
                            onWakeWord()
                        }
                        // Reset flag after a short delay
                        CoroutineScope(Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(2000)
                            _isWakeWordDetected.value = false
                        }
                    }
                }
            )
            porcupineManager?.start()
            isListening = true
        } catch (e: PorcupineException) {
            Log.e("WakeWord", "Failed to start: ${e.message}")
        }
    }

    fun stopListening() {
        porcupineManager?.stop()
        porcupineManager?.delete()
        porcupineManager = null
        isListening = false
    }

    fun isListening(): Boolean = isListening
}