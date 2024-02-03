package com.dacosys.assetControl.utils.settings.preferences

import android.content.SharedPreferences
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.entries.ConfEntry

class Preferences {
    companion object {

        lateinit var prefs: SharedPreferences

        fun resetLastUpdateDates(): Boolean {
            try {
                for (registryType in SyncRegistryType.getSyncDownload()) {
                    prefsCleanKey(registryType.confEntry!!.description)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                return false
            }
            return true
        }

        fun cleanPrefs(): Boolean {
            return try {
                prefs.edit().clear().apply()
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsGetByKey(key: String): Any? {
            return prefs.all[key]
        }

        fun prefsCleanKey(key: String): Boolean {
            return try {
                with(prefs.edit()) {
                    remove(key).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutLong(key: String, value: Long): Boolean {
            return try {
                with(prefs.edit()) {
                    putLong(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutInt(key: String, value: Int): Boolean {
            return try {
                with(prefs.edit()) {
                    putInt(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutBoolean(key: String, value: Boolean): Boolean {
            return try {
                with(prefs.edit()) {
                    putBoolean(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutString(key: String, value: String): Boolean {
            return try {
                with(prefs.edit()) {
                    putString(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutString(key: ArrayList<String>, value: String): Boolean {
            return try {
                with(prefs.edit()) {
                    for (k in key) {
                        putString(k, value).apply()
                    }
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutStringSet(key: String, value: Set<String>): Boolean {
            return try {
                with(prefs.edit()) {
                    putStringSet(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsGetStringSet(key: String, value: ArrayList<String>): Set<String>? {
            return try {
                prefs.getStringSet(key, value.toSet())
            } catch (ex: java.lang.Exception) {
                value.toSet()
            }
        }

        fun prefsGetString(p: ConfEntry): String {
            return prefsGetString(p.description, p.defaultValue as String)
        }

        fun prefsGetString(p: Preference): String {
            return prefsGetString(p.key, p.defaultValue as String)
        }

        private fun prefsGetString(key: String, defValue: String): String {
            return try {
                return when (val pref = prefsGetByKey(key)) {
                    is String -> pref
                    else -> pref?.toString() ?: defValue
                }
            } catch (ex: java.lang.Exception) {
                defValue
            }
        }

        fun prefsGetInt(p: ConfEntry): Int {
            return prefsGetInt(p.description, p.defaultValue.toString().toInt())
        }

        fun prefsGetInt(p: Preference): Int {
            return prefsGetInt(p.key, p.defaultValue as Int)
        }

        private fun prefsGetInt(key: String, defValue: Int): Int {
            return try {
                when (val pref = prefsGetByKey(key)) {
                    is Int -> pref
                    is Long -> pref.toInt()
                    is String -> pref.toInt()
                    else -> defValue
                }
            } catch (ex: java.lang.Exception) {
                return defValue
            }
        }

        fun prefsGetLong(p: Preference): Long {
            return prefsGetLong(p.key, p.defaultValue as Long)
        }

        private fun prefsGetLong(key: String, defValue: Long): Long {
            return try {
                when (val pref = prefsGetByKey(key)) {
                    is Long -> pref
                    is Int -> pref.toLong()
                    is String -> pref.toInt().toLong()
                    else -> defValue
                }
            } catch (ex: java.lang.Exception) {
                return defValue
            }
        }

        fun prefsGetFloat(p: Preference): Float {
            return prefsGetFloat(p.key, p.defaultValue as Float)
        }

        private fun prefsGetFloat(key: String, defValue: Float): Float {
            return try {
                when (val pref = prefsGetByKey(key)) {
                    is Float -> pref
                    is String -> pref.toFloat()
                    else -> defValue
                }
            } catch (ex: java.lang.Exception) {
                return defValue
            }
        }

        fun prefsGetBoolean(p: ConfEntry): Boolean {
            return prefsGetBoolean(p.description, p.defaultValue.toString().toBoolean())
        }

        fun prefsGetBoolean(p: Preference): Boolean {
            return prefsGetBoolean(p.key, p.defaultValue as Boolean)
        }

        private fun prefsGetBoolean(key: String, defValue: Boolean): Boolean {
            return try {
                when (val pref = prefsGetByKey(key)) {
                    is Boolean -> pref
                    is Int -> pref == 1
                    is String -> pref.toBoolean()
                    else -> defValue
                }
            } catch (ex: java.lang.Exception) {
                return defValue
            }
        }
    }
}