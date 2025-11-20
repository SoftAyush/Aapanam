package org.example.aapanam.di

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.Transacter
import kotlin.time.Instant
import org.example.aapanam.data.local.Database
import org.example.aapanam.data.model.PaymentMode
import org.example.aapanam.data.remote.FirebaseService
import org.example.aapanam.db.AppDatabase
import org.example.aapanam.repository.Repository
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(
        repositoryModule,
        databaseModule,
        firebaseModule,
        viewModelModule,
        platformModule
    )
}

val repositoryModule = module {
    single { Repository(get(), get(), get()) }
}

val databaseModule = module {
    single {
        AppDatabase(get())
    }

    single { Database(get()) }
    single<Transacter> { get<AppDatabase>() } // Provide AppDatabase as a Transacter
}

val firebaseModule = module {
    single { FirebaseService() }
}


expect val platformModule: Module
expect val viewModelModule: Module
