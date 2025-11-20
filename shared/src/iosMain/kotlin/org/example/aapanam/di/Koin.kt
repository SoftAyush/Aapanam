package org.example.aapanam.di

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.example.aapanam.data.local.DatabaseDriverFactory
import org.example.aapanam.presentation.AuthViewModel
import org.example.aapanam.presentation.CreditViewModel
import org.example.aapanam.presentation.DashboardViewModel
import org.example.aapanam.presentation.InventoryViewModel
import org.example.aapanam.presentation.LowStockViewModel
import org.example.aapanam.presentation.ReportsViewModel
import org.example.aapanam.presentation.SalesViewModel
import org.example.aapanam.presentation.SettingsViewModel
import org.example.aapanam.presentation.ThemeViewModel


actual val platformModule = module {
    single<SqlDriver> { DatabaseDriverFactory().createDriver() }
}

actual val viewModelModule = module {
    viewModelOf(::InventoryViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::SalesViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ThemeViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::CreditViewModel)
    viewModelOf(::LowStockViewModel)
    viewModelOf(::ReportsViewModel)
}