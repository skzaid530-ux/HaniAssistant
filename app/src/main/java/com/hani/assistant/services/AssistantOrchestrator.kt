package com.hani.assistant.services

import android.content.Context
import com.hani.assistant.repository.AppRepository
import com.hani.assistant.services.ai.OpenAiChatService
import com.hani.assistant.services.overlay.FloatingOverlayService
import com.hani.assistant.services.overlay.OverlayViewModel
import com.hani.assistant.services.speech.SpeechToTextEngine
import com.hani.assistant.services.speech.TextToSpeechEngine
import com.hani.assistant.services.wake.WakeWordEngine
import com.hani.assistant.utils.CommandProcessor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wakeWordEngine: WakeWordEngine,
    private val speechToText: SpeechToTextEngine,
    private val textToSpeech: TextToSpeechEngine,
    private val openAiChatService: OpenAiChatService,
    private val appRepository: AppRepository,
    private val overlayViewModel: OverlayViewModel,
    private val commandProcessor: CommandProcessor
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isActive = MutableStateFlow(true) // awake/sleep
    val isActive: StateFlow<Boolean> = _isActive

    private var isProcessing = false

    init {
        // Start wake word listening
        wakeWordEngine.startListening {
            if (_isActive.value) {
                onWakeWordDetected()
            }
        }
        // Set initial overlay state
        overlayViewModel.updateState(FloatingOverlayService.State.IDLE)
    }

    private fun onWakeWordDetected() {
        if (isProcessing) return
        isProcessing = true
        overlayViewModel.updateState(FloatingOverlayService.State.LISTENING)
        textToSpeech.stop() // stop any ongoing speech

        // Start listening for command
        speechToText.startListening(
            onResult = { command ->
                processCommand(command)
            },
            onError = {
                overlayViewModel.updateState(FloatingOverlayService.State.ERROR)
                isProcessing = false
                // After error, resume wake word
            }
        )
    }

    private fun processCommand(command: String) {
        overlayViewModel.updateState(FloatingOverlayService.State.THINKING)
        // Check for sleep command
        if (command.trim().equals("sleep", ignoreCase = true)) {
            goToSleep()
            textToSpeech.speak("Okay, going to sleep. Say Hani to wake me.")
            isProcessing = false
            return
        }

        // Process command via command processor (which handles app opening, calls, etc.)
        scope.launch {
            val response = commandProcessor.executeCommand(command)
            if (response != null) {
                // Command executed, provide response
                textToSpeech.speak(response)
                overlayViewModel.updateState(FloatingOverlayService.State.TALKING)
                // Save conversation
                appRepository.saveConversation(command, response)
                // After speaking, go back to idle
                scope.launch {
                    kotlinx.coroutines.delay(3000)
                    overlayViewModel.updateState(FloatingOverlayService.State.IDLE)
                    isProcessing = false
                }
            } else {
                // Not a direct command, use AI
                val history = appRepository.getAll()
                val chatHistory = history.map { Pair(it.userMessage, it.botResponse) }
                val result = openAiChatService.getChatResponse(command, chatHistory)
                result.onSuccess { reply ->
                    textToSpeech.speak(reply)
                    appRepository.saveConversation(command, reply)
                    overlayViewModel.updateState(FloatingOverlayService.State.TALKING)
                    scope.launch {
                        kotlinx.coroutines.delay(3000)
                        overlayViewModel.updateState(FloatingOverlayService.State.IDLE)
                        isProcessing = false
                    }
                }.onFailure {
                    textToSpeech.speak("Sorry, I had a problem.")
                    overlayViewModel.updateState(FloatingOverlayService.State.ERROR)
                    isProcessing = false
                }
            }
        }
    }

    private fun goToSleep() {
        _isActive.value = false
        overlayViewModel.updateState(FloatingOverlayService.State.SLEEPING)
        speechToText.stopListening()
        // Wake word still listens? Actually we want to keep wake word engine running, but only wake if active is false.
        // So we keep it running but check active flag in callback.
    }

    fun wakeUp() {
        _isActive.value = true
        overlayViewModel.updateState(FloatingOverlayService.State.IDLE)
        // Wake word is already listening
    }

    fun shutdown() {
        scope.cancel()
        wakeWordEngine.stopListening()
        speechToText.destroy()
        textToSpeech.shutdown()
    }
}