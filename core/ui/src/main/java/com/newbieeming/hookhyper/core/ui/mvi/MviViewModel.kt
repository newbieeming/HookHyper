package com.newbieeming.hookhyper.core.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<State : Any, Intent : Any, Effect : Any>(
    initialState: State,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<State> = mutableState.asStateFlow()

    private val effectChannel = Channel<Effect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun accept(intent: Intent) = onIntent(intent)

    protected abstract fun onIntent(intent: Intent)

    protected fun reduce(transform: State.() -> State) {
        mutableState.update(transform)
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
