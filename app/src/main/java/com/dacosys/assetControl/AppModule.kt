package com.dacosys.assetControl

import androidx.preference.PreferenceManager
import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.devices.jotter.Jotter
import com.dacosys.assetControl.devices.jotter.ScannerManager
import com.dacosys.assetControl.devices.jotter.event.ActivityEvent.Companion.scannerListenerEvents
import com.dacosys.assetControl.utils.Statics.Companion.INTERNAL_IMAGE_CONTROL_APP_ID
import com.dacosys.assetControl.viewModel.sync.SyncViewModel
import com.dacosys.imageControl.ImageControl
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