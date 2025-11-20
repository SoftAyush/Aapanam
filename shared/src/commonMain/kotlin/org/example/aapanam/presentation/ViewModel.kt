package org.example.aapanam.presentation

import kotlinx.coroutines.CoroutineScope

expect open class ViewModel() {
    val coroutineScope: CoroutineScope
}
