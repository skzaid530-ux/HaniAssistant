package com.hani.assistant.services.overlay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayStateManager @Inject constructor()  {
    private val _state = MutableStateFlow(FloatingOverlayService.State.IDLE)
    val state: StateFlow<FloatingOverlayService.State> = _state

    fun updateState(newState: FloatingOverlayService.State) {
        _state.value = newState
    }
}