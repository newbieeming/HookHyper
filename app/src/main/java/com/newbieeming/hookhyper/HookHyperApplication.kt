package com.newbieeming.hookhyper

import android.app.Application
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HookHyperApplication : Application() {
    // Hilt injects Application fields before onCreate.
    @Suppress("LateinitUsage")
    @Inject lateinit var preferences: HookPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        // The helper also delivers service binders received before Application.onCreate.
        preferences.connect()
    }
}
