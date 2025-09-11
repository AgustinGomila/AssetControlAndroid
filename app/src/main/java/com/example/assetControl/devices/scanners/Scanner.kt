package com.example.assetControl.devices.scanners

import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.assetControl.devices.scanners.Collector.Companion.collectorType
import com.example.assetControl.devices.scanners.collector.CollectorType
import com.example.assetControl.devices.scanners.honeywell.Honeywell
import com.example.assetControl.devices.scanners.honeywell.HoneywellNative
import com.example.assetControl.devices.scanners.zebra.Zebra
import java.lang.ref.WeakReference

open class Scanner {
    interface ScannerListener {
        fun scannerCompleted(scanCode: String)
    }

    constructor()

    constructor(activity: AppCompatActivity) {
        build(activity)
    }

    private var scannerDevice: Scanner? = null
    private fun build(activity: AppCompatActivity): Scanner? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        Log.i(javaClass.simpleName, "SCANNER CONNECTED Manufacturer: $manufacturer, Model: $model")

        try {
            scannerDevice = when (collectorType) {
                CollectorType.honeywell -> Honeywell(activity)
                CollectorType.honeywellNative -> HoneywellNative(WeakReference(activity))
                CollectorType.zebra -> Zebra(activity)
                else -> null
            }
        } catch (e: NoClassDefFoundError) {
            //handle carefully
        }
        return scannerDevice
    }

    fun activityName(): String {
        val scanner = scannerDevice ?: return ""
        return try {
            when (scanner) {
                is Honeywell -> scanner.activityName
                is HoneywellNative -> scanner.activityName
                is Zebra -> scanner.activityName
                else -> ""
            }
        } catch (e: NoClassDefFoundError) {
            //handle carefully
            ""
        }
    }

    fun onResume() {
        val scanner = scannerDevice ?: return
        try {
            when (scanner) {
                is Honeywell -> scanner.pause()
                is HoneywellNative -> scanner.pause()
                is Zebra -> scanner.pause()
            }
        } catch (e: NoClassDefFoundError) {
            //handle carefully
        }
    }

    fun onPause() {
        val scanner = scannerDevice ?: return
        try {
            when (scanner) {
                is Honeywell -> scanner.pause()
                is HoneywellNative -> scanner.pause()
                is Zebra -> scanner.pause()
            }
        } catch (e: NoClassDefFoundError) {
            //handle carefully
        }
    }

    fun onDestroy() {
        val scanner = scannerDevice ?: return
        try {
            when (scanner) {
                is HoneywellNative -> scanner.destroy()
            }
        } catch (e: NoClassDefFoundError) {
            //handle carefully
        }
        // release resources!
        scannerDevice = null
    }

    fun trigger() {
        val scanner = scannerDevice ?: return
        try {
            when (scanner) {
                is Honeywell -> scanner.triggerScanner()
                is HoneywellNative -> scanner.triggerScanner()
                is Zebra -> scanner.triggerScanner()
            }
        } catch (e: NoClassDefFoundError) {
            //handle carefully
        }
    }

    fun lockScanner(lock: Boolean) {
        val scanner = scannerDevice ?: return
        try {
            when (scanner) {
                is Honeywell -> scanner.lockScannerEvent = lock
                is HoneywellNative -> scanner.lockScannerEvent = lock
                is Zebra -> scanner.lockScannerEvent = lock
            }
        } catch (e: NoClassDefFoundError) {
            //handle carefully
        }
    }
}