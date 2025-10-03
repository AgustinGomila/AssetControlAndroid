package com.example.assetControl

import androidx.preference.PreferenceManager
import com.dacosys.imageControl.ImageControl
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.devices.deviceLifecycle.DeviceLifecycle
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.deviceLifecycle.event.ActivityEvent.Companion.scannerListenerEvents
import com.example.assetControl.utils.Statics.Companion.INTERNAL_IMAGE_CONTROL_APP_ID
import com.example.assetControl.utils.settings.preferences.SettingsRepository
import com.example.assetControl.utils.settings.preferences.SettingsViewModel
import com.example.assetControl.viewModel.sync.SyncViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { PreferenceManager.getDefaultSharedPreferences(context) }
    single { SettingsRepository() }

    viewModel { SyncViewModel() }
    viewModel { SettingsViewModel() }

    /** Setup ImageControl app identification */
    single { ImageControl.Builder(INTERNAL_IMAGE_CONTROL_APP_ID).build() }

    /** DeviceLifecycle! */
    single {
        DeviceLifecycle.Builder(androidApplication())
            .setLogEnable(true)
            .setActivityEventFilter(scannerListenerEvents)
            .setLifecycleListener(ScannerManager)
            .build()
    }
}