package com.dacosys.assetControl.utils

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.assetControl.utils.settings.entries.ConfEntry

class Preferences {
    companion object {

        var prefs: SharedPreferences? = null

        fun startPrefs() {
            prefs = PreferenceManager.getDefaultSharedPreferences(AssetControlApp.getContext())
        }

        fun prefsIsInitialized(): Boolean {
            return prefs != null
        }

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
            if (prefs == null) {
                return false
            }

            return try {
                prefs!!.edit().clear().apply()
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsGetByKey(key: String): Any? {
            if (prefs == null) {
                return null
            }

            return prefs!!.all[key]
        }

        fun prefsCleanKey(key: String): Boolean {
            if (prefs == null) {
                return false
            }

            return try {
                with(prefs!!.edit()) {
                    remove(key).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutLong(key: String, value: Long): Boolean {
            if (prefs == null) {
                return false
            }

            return try {
                with(prefs!!.edit()) {
                    putLong(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutInt(key: String, value: Int): Boolean {
            if (prefs == null) {
                return false
            }

            return try {
                with(prefs!!.edit()) {
                    putInt(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutBoolean(key: String, value: Boolean): Boolean {
            if (prefs == null) {
                return false
            }

            return try {
                with(prefs!!.edit()) {
                    putBoolean(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutString(key: String, value: String): Boolean {
            if (prefs == null) {
                return false
            }

            return try {
                with(prefs!!.edit()) {
                    putString(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsPutString(key: ArrayList<String>, value: String): Boolean {
            if (prefs == null) {
                return false
            }

            return try {
                with(prefs!!.edit()) {
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
            if (prefs == null) {
                return false
            }

            return try {
                with(prefs!!.edit()) {
                    putStringSet(key, value).apply()
                }
                true
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                false
            }
        }

        fun prefsGetStringSet(key: String, value: ArrayList<String>): Set<String>? {
            if (prefs == null) {
                return value.toSet()
            }

            return try {
                prefs!!.getStringSet(key, value.toSet())
            } catch (ex: java.lang.Exception) {
                value.toSet()
            }
        }

        fun prefsGetString(p: ConfEntry): String {
            return privPrefsGetString(p.description, p.defaultValue as String)
        }

        fun prefsGetString(p: Preference): String {
            return privPrefsGetString(p.key, p.defaultValue as String)
        }

        private fun privPrefsGetString(key: String, defValue: String): String {
            if (prefs == null) {
                return defValue
            }

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
            if (prefs == null) {
                return defValue
            }

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
            if (prefs == null) {
                return defValue
            }

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
            if (prefs == null) {
                return defValue
            }

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
            return privPrefsGetBoolean(p.description, p.defaultValue.toString().toBoolean())
        }

        fun prefsGetBoolean(p: Preference): Boolean {
            return privPrefsGetBoolean(p.key, p.defaultValue as Boolean)
        }

        private fun privPrefsGetBoolean(key: String, defValue: Boolean): Boolean {
            if (prefs == null) {
                return defValue
            }

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