package com.luckyhan.studio.mokaeditor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MokaDebounce(private val interval : Long, private val coroutineScope : CoroutineScope) {
    var debounceJob : Job? = null

    fun request(block : ()->Unit){
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(interval)
            block()
        }
    }
}