package com.example.assetControl.utils.settings.config

import android.util.Log
import com.example.assetControl.AssetControlApp
import com.example.assetControl.AssetControlApp.Companion.appName
import com.example.assetControl.AssetControlApp.Companion.prefs
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.BuildConfig
import com.example.assetControl.R
import com.example.assetControl.network.clientPackages.ClientPackagesProgress
import com.example.assetControl.network.clientPackages.GetClientPackages
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.errorLog.ErrorLog
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
                mainJson.has(appName) && mode != QRConfigType.QRConfigClientAccount -> appName
                else -> ""
            }

            if (mainTag.isEmpty()) {
                onRequestProgress.invoke(
                    ClientPackagesProgress(
                        status = ProgressStatus.crashed,
                        msg = AssetControlApp.context.getString(R.string.invalid_code)
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
                                msg = AssetControlApp.context.getString(R.string.invalid_code)
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
                                QRConfigType.QRConfigImageControl -> AssetControlApp.context
                                    .getString(R.string.imagecontrol_configured)

                                QRConfigType.QRConfigWebservice -> AssetControlApp.context
                                    .getString(R.string.server_configured)

                                else -> AssetControlApp.context
                                    .getString(R.string.configuration_applied)
                            }
                        )
                    )
                }

                else -> {
                    onRequestProgress.invoke(
                        ClientPackagesProgress(
                            status = ProgressStatus.crashed,
                            msg = AssetControlApp.context.getString(R.string.invalid_code)
                        )
                    )
                }
            }
        }

        fun getBarcodeForConfig(ps: ArrayList<Preference>, mainTag: String): String {
            val jsonObject = JSONObject()

            for (p in ps) {
                if (p.defaultValue is Int) {
                    val value = sr.prefsGetInt(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is Boolean) {
                    val value = sr.prefsGetBoolean(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is String) {
                    val value = sr.prefsGetString(p)
                    if (value != p.defaultValue && value.isNotEmpty()) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is Long) {
                    val value = sr.prefsGetLong(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is Float) {
                    val value = sr.prefsGetFloat(p)
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

                with(prefs.edit()) {
                    val tempPref = prefs.all[prefName]
                    if (tempPref != null) {
                        try {
                            when (tempPref) {
                                is String -> putString(prefName, conf.getString(prefName))
                                is Boolean -> putBoolean(prefName, conf.getBoolean(prefName))
                                is Int -> putInt(prefName, conf.getInt(prefName))
                                is Float -> putFloat(prefName, conf.getDouble(prefName).toFloat())
                                is Long -> putLong(prefName, conf.getLong(prefName))
                                else -> putString(prefName, conf.getString(prefName))
                            }.apply()
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
                                    is String -> putString(prefName, conf.getString(prefName))
                                    is Boolean -> putBoolean(prefName, conf.getBoolean(prefName))
                                    is Int -> putInt(prefName, conf.getInt(prefName))
                                    is Float -> putFloat(prefName, conf.getDouble(prefName).toFloat())
                                    is Long -> putLong(prefName, conf.getLong(prefName))
                                    else -> putString(prefName, conf.getString(prefName))
                                }.apply()
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
                }
            }
        }

        fun setDebugConfigValues() {
            // VALORES POR DEFECTO, SOLO PARA DEBUG
            if (Statics.isDebuggable() || BuildConfig.DEBUG) {
                // region ASSET CONTROL WEBSERVICE
                if (svm.wsUrl.isEmpty()) {
                    svm.wsUrl = Preference.acWsServer.debugValue as String
                }

                if (svm.wsNamespace.isEmpty()) {
                    svm.wsNamespace = Preference.acWsNamespace.debugValue as String
                }

                if (svm.acWsUser.isEmpty()) {
                    svm.acWsUser = Preference.acWsUser.debugValue as String
                }

                if (svm.acWsPass.isEmpty()) {
                    svm.acWsPass = Preference.acWsPass.debugValue as String
                }

                if (svm.acUser.isEmpty()) {
                    svm.acUser = Preference.acUser.debugValue as String
                }

                if (svm.acPass.isEmpty()) {
                    svm.acPass = Preference.acPass.debugValue as String
                }
                // endregion

                // region ASSET CONTROL MAINTENANCE WEBSERVICE
                if (svm.acMantWsServer.isEmpty()) {
                    svm.acMantWsServer = Preference.acMantWsServer.debugValue as String
                }

                if (svm.wsMantNamespace.isEmpty()) {
                    svm.wsMantNamespace = Preference.acMantWsNamespace.debugValue as String
                }

                if (svm.acMantWsUser.isEmpty()) {
                    svm.acMantWsUser = Preference.acMantWsUser.debugValue as String
                }

                if (svm.acMantWsPass.isEmpty()) {
                    svm.acMantWsPass = Preference.acMantWsPass.debugValue as String
                }

                if (svm.acMantUser.isEmpty()) {
                    svm.acMantUser = Preference.acMantUser.debugValue as String
                }

                if (svm.acMantPass.isEmpty()) {
                    svm.acMantPass = Preference.acMantPass.debugValue as String
                }
                // endregion

                // region IMAGE CONTROL WEBSERVICE
                if (svm.wsIcUrl.isEmpty()) {
                    svm.wsIcUrl = Preference.icWsServer.debugValue as String
                }

                if (svm.wsIcNamespace.isEmpty()) {
                    svm.wsIcNamespace = Preference.icWsNamespace.debugValue as String
                }

                if (svm.wsIcUser.isEmpty()) {
                    svm.wsIcUser = Preference.icWsUser.debugValue as String
                }

                if (svm.wsIcPass.isEmpty()) {
                    svm.wsIcPass = Preference.icWsPass.debugValue as String
                }

                if (svm.icUser.isEmpty()) {
                    svm.icUser = Preference.icUser.debugValue as String
                }

                if (svm.icPass.isEmpty()) {
                    svm.icPass = Preference.icPass.debugValue as String
                }

                if (svm.icUser.isEmpty()) {
                    svm.icUser = Preference.icUser.debugValue as String
                }

                if (svm.icPass.isEmpty()) {
                    svm.icPass = Preference.icPass.debugValue as String
                }
                // endregion
            }
        }
    }
}