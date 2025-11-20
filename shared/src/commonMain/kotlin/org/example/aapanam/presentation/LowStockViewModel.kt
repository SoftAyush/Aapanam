package org.example.aapanam.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.aapanam.data.model.Item
import org.example.aapanam.repository.Repository

class LowStockViewModel(private val repository: Repository) : ViewModel() {

    private val _lowStockItems = MutableStateFlow<List<Item>>(emptyList())
    val lowStockItems = _lowStockItems.asStateFlow()

    init {
        coroutineScope.launch {
            repository.getLowStockItems(10).collect {
                _lowStockItems.value = it
            }
        }
    }
}
