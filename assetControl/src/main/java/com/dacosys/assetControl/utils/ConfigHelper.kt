package com.dacosys.assetControl.utils

import android.util.Log
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.network.clientPackages.ClientPackagesProgress
import com.dacosys.assetControl.network.clientPackages.GetClientPackages
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.preferences.Preferences
import com.dacosys.assetControl.utils.preferences.Repository
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.assetControl.utils.settings.QRConfigType
import org.json.JSONObject

class ConfigHelper {
    interface TaskConfigEnded {
        fun onTaskConfigEnded(result: Boolean, msg: String)
    }

    companion object {
        fun getConfig(
            email: String,
            password: String,
            installationCode: String,
            onRequestProgress: (ClientPackagesProgress) -> Unit = {},
        ) {
            if (email.trim().isNotEmpty() && password.trim().isNotEmpty()) {
                GetClientPackages(
                    email = email,
                    password = password,
                    installationCode = installationCode,
                    onProgress = onRequestProgress
                )
            }
        }

        fun getConfigFromScannedCode(
            scanCode: String,
            mode: QRConfigType,
            onRequestProgress: (ClientPackagesProgress) -> Unit = {},
        ) {
            val mainJson = JSONObject(scanCode)
            val mainTag = when {
                mainJson.has("config") && mode == QRConfigType.QRConfigClientAccount -> "config"
                mainJson.has(Statics.appName) && mode != QRConfigType.QRConfigClientAccount -> Statics.appName
                else -> ""
            }

            if (mainTag.isEmpty()) {
                onRequestProgress.invoke(
                    ClientPackagesProgress(
                        status = ProgressStatus.crashed,
                        msg = AssetControlApp.getContext().getString(R.string.invalid_code)
                    )
                )
                return
            }

            val confJson = mainJson.getJSONObject(mainTag)

            when (mode) {
                QRConfigType.QRConfigClientAccount -> {
                    // Package Client Setup
                    val installationCode =
                        if (confJson.has(Preference.installationCode.key)) confJson.getString(
                            Preference.installationCode.key
                        ) else ""
                    val email =
                        if (confJson.has(Preference.clientEmail.key)) confJson.getString(Preference.clientEmail.key) else ""
                    val password =
                        if (confJson.has(Preference.clientPassword.key)) confJson.getString(
                            Preference.clientPassword.key
                        ) else ""

                    if (email.trim().isNotEmpty() && password.trim().isNotEmpty()) {
                        getConfig(
                            email = email,
                            password = password,
                            installationCode = installationCode,
                            onRequestProgress = onRequestProgress
                        )
                    } else {
                        onRequestProgress.invoke(
                            ClientPackagesProgress(
                                status = ProgressStatus.crashed,
                                clientEmail = email,
                                clientPassword = password,
                                msg = AssetControlApp.getContext().getString(R.string.invalid_code)
                            )
                        )
                    }
                }

                QRConfigType.QRConfigWebservice, QRConfigType.QRConfigApp, QRConfigType.QRConfigImageControl -> {
                    tryToLoadConfig(confJson)
                    onRequestProgress.invoke(
                        ClientPackagesProgress(
                            status = ProgressStatus.success,
                            msg = when (mode) {
                                QRConfigType.QRConfigImageControl -> AssetControlApp.getContext()
                                    .getString(R.string.imagecontrol_configured)

                                QRConfigType.QRConfigWebservice -> AssetControlApp.getContext()
                                    .getString(R.string.server_configured)

                                else -> AssetControlApp.getContext()
                                    .getString(R.string.configuration_applied)
                            }
                        )
                    )
                }

                else -> {
                    onRequestProgress.invoke(
                        ClientPackagesProgress(
                            status = ProgressStatus.crashed,
                            msg = AssetControlApp.getContext().getString(R.string.invalid_code)
                        )
                    )
                }
            }
        }

        fun getBarcodeForConfig(ps: ArrayList<Preference>, mainTag: String): String {
            val jsonObject = JSONObject()

            for (p in ps) {
                if (p.defaultValue is Int) {
                    val value = Preferences.prefsGetInt(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is Boolean) {
                    val value = Preferences.prefsGetBoolean(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is String) {
                    val value = Preferences.prefsGetString(p)
                    if (value != p.defaultValue && value.isNotEmpty()) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is Long) {
                    val value = Preferences.prefsGetLong(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is Float) {
                    val value = Preferences.prefsGetFloat(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                }
            }

            val jsonRes = JSONObject()
            jsonRes.put(mainTag, jsonObject)

            Log.d(this::class.java.simpleName, jsonRes.toString())
            return jsonRes.toString()
        }

        private fun tryToLoadConfig(conf: JSONObject) {
            val availablePref = Preference.getConfigPreferences()
            for (prefName in conf.keys()) {

                // No está permitido cargar configuraciones de cliente por esta vía.
                if (!availablePref.any { it.key == prefName }) {
                    continue
                }

                val p = Preferences.prefs.edit()
                val tempPref = Preferences.prefs.all[prefName]
                if (tempPref != null) {
                    try {
                        when (tempPref) {
                            is String -> p.putString(prefName, conf.getString(prefName))
                            is Boolean -> p.putBoolean(prefName, conf.getBoolean(prefName))
                            is Int -> p.putInt(prefName, conf.getInt(prefName))
                            is Float -> p.putFloat(prefName, conf.getDouble(prefName).toFloat())
                            is Long -> p.putLong(prefName, conf.getLong(prefName))
                            else -> p.putString(prefName, conf.getString(prefName))
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        Log.e(
                            this::class.java.simpleName,
                            "Imposible convertir valor de configuración: $prefName"
                        )
                        ErrorLog.writeLog(null, "tryToLoadConfig", ex)
                    }
                } else {
                    val pref = Preference.getByKey(prefName)
                    if (pref != null) {
                        try {
                            when (pref.defaultValue) {
                                is String -> p.putString(prefName, conf.getString(prefName))
                                is Boolean -> p.putBoolean(prefName, conf.getBoolean(prefName))
                                is Int -> p.putInt(prefName, conf.getInt(prefName))
                                is Float -> p.putFloat(prefName, conf.getDouble(prefName).toFloat())
                                is Long -> p.putLong(prefName, conf.getLong(prefName))
                                else -> p.putString(prefName, conf.getString(prefName))
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            Log.e(
                                this::class.java.simpleName,
                                "Imposible convertir valor de configuración: $prefName"
                            )
                            ErrorLog.writeLog(null, "tryToLoadConfig", ex)
                        }
                    }
                }
                p.apply()
            }
        }

        fun setDebugConfigValues() {
            // VALORES POR DEFECTO, SÓLO PARA DEBUG
            if (Statics.isDebuggable() || BuildConfig.DEBUG) {
                val x = Preferences.prefs.edit()

                // region ASSET CONTROL WEBSERVICE
                if (Repository.wsUrl.isEmpty()) {
                    x.putString(
                        Preference.acWsServer.key, Preference.acWsServer.debugValue as String?
                    )
                }

                if (Repository.wsNamespace.isEmpty()) {
                    x.putString(
                        Preference.acWsNamespace.key,
                        Preference.acWsNamespace.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.acWsUser).isEmpty()) {
                    x.putString(
                        Preference.acWsUser.key, Preference.acWsUser.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.acWsPass).isEmpty()) {
                    x.putString(
                        Preference.acWsPass.key, Preference.acWsPass.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.acUser).isEmpty()) {
                    x.putString(
                        Preference.acUser.key, Preference.acUser.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.acPass).isEmpty()) {
                    x.putString(
                        Preference.acPass.key, Preference.acPass.debugValue as String?
                    )
                }
                // endregion

                // region ASSET CONTROL MANTEINANCE WEBSERVICE
                if (Repository.wsMantUrl.isEmpty()) {
                    x.putString(
                        Preference.acMantWsServer.key,
                        Preference.acMantWsServer.debugValue as String?
                    )
                }

                if (Repository.wsMantNamespace.isEmpty()) {
                    x.putString(
                        Preference.acMantWsNamespace.key,
                        Preference.acMantWsNamespace.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.acMantWsUser).isEmpty()) {
                    x.putString(
                        Preference.acMantWsUser.key,
                        Preference.acMantWsUser.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.acMantWsPass).isEmpty()) {
                    x.putString(
                        Preference.acMantWsPass.key,
                        Preference.acMantWsPass.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.acMantUser).isEmpty()) {
                    x.putString(
                        Preference.acMantUser.key, Preference.acMantUser.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.acMantPass).isEmpty()) {
                    x.putString(
                        Preference.acMantPass.key, Preference.acMantPass.debugValue as String?
                    )
                }
                // endregion

                // region IMAGE CONTROL WEBSERVICE
                if (Repository.wsIcUrl.isEmpty()) {
                    x.putString(
                        Preference.icWsServer.key, Preference.icWsServer.debugValue as String?
                    )
                }

                if (Repository.wsIcNamespace.isEmpty()) {
                    x.putString(
                        Preference.icWsNamespace.key,
                        Preference.icWsNamespace.debugValue as String?
                    )
                }

                if (Repository.wsIcUser.isEmpty()) {
                    x.putString(
                        Preference.icWsUser.key, Preference.icWsUser.debugValue as String?
                    )
                }

                if (Repository.wsIcPass.isEmpty()) {
                    x.putString(
                        Preference.icWsPass.key, Preference.icWsPass.debugValue as String?
                    )
                }

                if (Repository.icUser.isEmpty()) {
                    x.putString(
                        Preference.icUser.key, Preference.icUser.debugValue as String?
                    )
                }

                if (Repository.icPass.isEmpty()) {
                    x.putString(
                        Preference.icPass.key, Preference.icPass.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.icUser).isEmpty()) {
                    x.putString(
                        Preference.icUser.key, Preference.icUser.debugValue as String?
                    )
                }

                if (Preferences.prefsGetString(Preference.icPass).isEmpty()) {
                    x.putString(
                        Preference.icPass.key, Preference.icPass.debugValue as String?
                    )
                }
                // endregion

                run {
                    x.apply()
                }
            }
        }
    }
}