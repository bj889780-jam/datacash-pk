package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class DataCashApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Prevent app crashes due to uncaught background or non-critical thread exceptions
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("DataCashApp", "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)
            // If exception occurs on main thread, forward to default handler to prevent ANR/zombie state,
            // otherwise swallow background thread crashes to keep app running smoothly.
            if (thread == android.os.Looper.getMainLooper().thread) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(this)
                } catch (e: Throwable) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:123456789012:android:1234567890abcdef")
                        .setApiKey("AIzaSyDummyApiKeyDataCashPKApp")
                        .setProjectId("datacash-pk-app")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            }
        } catch (e: Throwable) {
            Log.e("DataCashApp", "Firebase init prevented crash: ${e.message}")
        }
    }
}
