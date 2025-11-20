package org.example.aapanam.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

actual open class ViewModel {
    actual val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
}
