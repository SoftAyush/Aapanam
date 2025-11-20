package org.example.aapanam.presentation

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@Composable
actual inline fun <reified T : ViewModel> getViewModel(): T = koinInject()