package com.dacosys.assetControl.utils.scanners

import android.util.Log
import com.dacosys.assetControl.utils.settings.collectorType.CollectorType
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences

class Collector {
    companion object {
        // Este flag es para reinicializar el colector después de cambiar en Settings.
        var collectorTypeChanged = false

        val collectorType: CollectorType
            get() {
                return try {
                    return when (val pref = Preferences.prefsGetByKey(Preference.collectorType.key)
                        ?: CollectorType.none) {
                        is CollectorType -> pref
                        is String -> {
                            val id: Int = if (pref.isEmpty()) 0 else pref.toInt()
                            CollectorType.getById(id)
                        }

                        is Int, is Long -> {
                            CollectorType.getById(pref as Int)
                        }

                        else -> {
                            CollectorType.none
                        }
                    }
                } catch (ex: java.lang.Exception) {
                    Log.e(this::class.java.simpleName, ex.message.toString())
                    Preferences.prefsCleanKey(Preference.collectorType.key)
                    CollectorType.none
                }
            }

        fun isNfcRequired(): Boolean {
            return Preferences.prefsGetBoolean(Preference.useNfc)
        }
    }
}