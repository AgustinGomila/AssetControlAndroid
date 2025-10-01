package com.example.assetControl.devices.scanners

import android.util.Log
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.devices.scanners.collector.CollectorType
import com.example.assetControl.utils.settings.config.Preference

class Collector {
    companion object {
        // Este flag es para reinicializar el colector después de cambiar en Settings.
        var collectorTypeChanged = false
        var collectorType: CollectorType
            get() {
                val pref = sr.prefsGetByKey(Preference.collectorType.key)
                return try {
                    return when (pref) {
                        is CollectorType -> pref
                        is String -> CollectorType.getById(if (pref.isEmpty()) 0 else pref.toInt())
                        is Int -> CollectorType.getById(pref)
                        is Long -> CollectorType.getById(pref.toInt())
                        else -> CollectorType.none
                    }
                } catch (ex: java.lang.Exception) {
                    Log.e(this::class.java.simpleName, ex.message.toString())
                    sr.prefsCleanKey(Preference.collectorType.key)
                    CollectorType.none
                }
            }
            set(value) {
                sr.prefsPutString(Preference.collectorType.key, value.id.toString())
            }

        fun isNfcRequired(): Boolean {
            return sr.prefsGetBoolean(Preference.useNfc)
        }
    }
}