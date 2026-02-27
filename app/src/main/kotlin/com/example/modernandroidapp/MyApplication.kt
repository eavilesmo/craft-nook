package com.example.modernandroidapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Craft Nook.
 *
 * Marked with @HiltAndroidApp to enable Hilt dependency injection across the application.
 * This allows Hilt to generate all necessary DI components and initialize the dependency graph
 * when the application starts.
 *
 * All other Application initialization (analytics, crash reporting, etc.) can be added here.
 */
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO: Initialize third-party libraries (Firebase, Sentry, etc.)
        // TODO: Configure logging and analytics
    }
}
