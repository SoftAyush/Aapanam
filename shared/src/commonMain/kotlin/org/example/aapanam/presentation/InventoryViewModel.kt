package org.example.aapanam.presentation

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.aapanam.data.model.Item
import org.example.aapanam.repository.Repository

class InventoryViewModel(private val repository: Repository) : ViewModel() {

    val items: StateFlow<List<Item>> = repository.getAllItems()
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(item: Item) {
        coroutineScope.launch {
            repository.insertItem(item)
        }
    }

    fun updateItem(item: Item) {
        coroutineScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(itemId: Long) {
        coroutineScope.launch {
            repository.deleteItem(itemId)
        }
    }
}