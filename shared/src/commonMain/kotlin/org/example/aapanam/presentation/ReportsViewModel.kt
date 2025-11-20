package org.example.aapanam.presentation


import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.example.aapanam.data.model.SalesSummary
import org.example.aapanam.repository.Repository

class ReportsViewModel(private val repository: Repository) : ViewModel() {

    val salesSummary: StateFlow<SalesSummary> = repository.getSalesSummary()
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), SalesSummary.empty())

}
