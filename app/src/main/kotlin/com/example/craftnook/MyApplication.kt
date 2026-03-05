package com.example.craftnook

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO: Initialize third-party libraries (Firebase, Sentry, etc.)
        // TODO: Configure logging and analytics
    }
}
