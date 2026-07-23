package com.hani.assistant.services.overlay

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OverlayViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(FloatingOverlayService.State.IDLE)
    val state: StateFlow<FloatingOverlayService.State> = _state

    fun updateState(newState: FloatingOverlayService.State) {
        _state.value = newState
    }
}