package com.example.assetControl

import androidx.preference.PreferenceManager
import com.dacosys.imageControl.ImageControl
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.devices.jotter.Jotter
import com.example.assetControl.devices.jotter.ScannerManager
import com.example.assetControl.devices.jotter.event.ActivityEvent.Companion.scannerListenerEvents
import com.example.assetControl.utils.Statics.Companion.INTERNAL_IMAGE_CONTROL_APP_ID
import com.example.assetControl.viewModel.sync.SyncViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { PreferenceManager.getDefaultSharedPreferences(context) }
    viewModel { SyncViewModel() }

    /** Setup ImageControl app identification */
    single { ImageControl.Builder(INTERNAL_IMAGE_CONTROL_APP_ID).build() }

    /** Jotter! */
    single {
        Jotter.Builder(androidApplication())
            .setLogEnable(true)
            .setActivityEventFilter(scannerListenerEvents)
            .setLifecycleListener(ScannerManager)
            .build()
    }
}