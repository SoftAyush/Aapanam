package org.example.aapanam

import android.app.Application
import org.example.aapanam.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AapanamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@AapanamApplication)
        }
    }
}