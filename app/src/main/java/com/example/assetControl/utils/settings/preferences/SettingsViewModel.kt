package com.example.assetControl.utils.settings.preferences

import androidx.lifecycle.ViewModel
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.utils.settings.config.Preference

class SettingsViewModel : ViewModel() {
    var clientEmail: String
        get() = sr.prefsGetString(Preference.clientEmail)
        set(value) {
            sr.prefsPutString(Preference.clientEmail.key, value)
        }

    var clientPassword: String
        get() = sr.prefsGetString(Preference.clientPassword)
        set(value) {
            sr.prefsPutString(Preference.clientPassword.key, value)
        }

    var clientPackage: String
        get() = sr.prefsGetString(Preference.clientPackage)
        set(value) {
            sr.prefsPutString(Preference.clientPackage.key, value)
        }

    var installationCode: String
        get() = sr.prefsGetString(Preference.installationCode)
        set(value) {
            sr.prefsPutString(Preference.installationCode.key, value)
        }

    var acWsPass: String
        get() = sr.prefsGetString(Preference.acWsPass)
        set(value) {
            sr.prefsPutString(Preference.acWsPass.key, value)
        }

    var acUser: String
        get() = sr.prefsGetString(Preference.acUser)
        set(value) {
            sr.prefsPutString(Preference.acUser.key, value)
        }

    var acPass: String
        get() = sr.prefsGetString(Preference.acPass)
        set(value) {
            sr.prefsPutString(Preference.acPass.key, value)
        }

    var acWsUser: String
        get() = sr.prefsGetString(Preference.acWsUser)
        set(value) {
            sr.prefsPutString(Preference.acWsUser.key, value)
        }

    var wsUrl: String
        get() = sr.prefsGetString(Preference.acWsServer)
        set(value) {
            sr.prefsPutString(Preference.acWsServer.key, value)
        }

    var wsNamespace: String
        get() = sr.prefsGetString(Preference.acWsNamespace)
        set(value) {
            sr.prefsPutString(Preference.acWsNamespace.key, value)
        }

    var urlPanel: String
        get() = sr.prefsGetString(Preference.urlPanel)
        set(value) {
            sr.prefsPutString(Preference.urlPanel.key, value)
        }

    var connectionTimeout: Int
        get() = sr.prefsGetInt(Preference.connectionTimeout)
        set(value) {
            sr.prefsPutInt(Preference.connectionTimeout.key, value)
        }

    var wsProxy: String
        get() = sr.prefsGetString(Preference.acWsProxy)
        set(value) {
            sr.prefsPutString(Preference.acWsProxy.key, value)
        }

    var wsProxyPort: Int
        get() = sr.prefsGetInt(Preference.acWsProxyPort)
        set(value) {
            sr.prefsPutInt(Preference.acWsProxyPort.key, value)
        }

    var wsUseProxy: Boolean
        get() = sr.prefsGetBoolean(Preference.acWsUseProxy)
        set(value) {
            sr.prefsPutBoolean(Preference.acWsUseProxy.key, value)
        }

    var wsProxyUser: String
        get() = sr.prefsGetString(Preference.acWsProxyUser)
        set(value) {
            sr.prefsPutString(Preference.acWsProxyUser.key, value)
        }

    var wsProxyPass: String
        get() = sr.prefsGetString(Preference.acWsProxyPass)
        set(value) {
            sr.prefsPutString(Preference.acWsProxyPass.key, value)
        }

    val wsUrlCron: String
        get() = wsNamespace.replace("/s1", "")

    var acMantWsServer: String
        get() = sr.prefsGetString(Preference.acMantWsServer)
        set(value) {
            sr.prefsPutString(Preference.acMantWsServer.key, value)
        }

    var wsMantNamespace: String
        get() = sr.prefsGetString(Preference.acMantWsNamespace)
        set(value) {
            sr.prefsPutString(Preference.acMantWsNamespace.key, value)
        }

    var acMantPass: String
        get() = sr.prefsGetString(Preference.acMantPass)
        set(value) {
            sr.prefsPutString(Preference.acMantPass.key, value)
        }

    var acMantUser: String
        get() = sr.prefsGetString(Preference.acMantUser)
        set(value) {
            sr.prefsPutString(Preference.acMantUser.key, value)
        }

    var acMantWsPass: String
        get() = sr.prefsGetString(Preference.acMantWsPass)
        set(value) {
            sr.prefsPutString(Preference.acMantWsPass.key, value)
        }

    var acMantWsUser: String
        get() = sr.prefsGetString(Preference.acMantWsUser)
        set(value) {
            sr.prefsPutString(Preference.acMantWsUser.key, value)
        }

    var wsMantProxy: String
        get() = sr.prefsGetString(Preference.acMantWsProxy)
        set(value) {
            sr.prefsPutString(Preference.acMantWsProxy.key, value)
        }

    var wsMantProxyPort: Int
        get() = sr.prefsGetInt(Preference.acMantWsProxyPort)
        set(value) {
            sr.prefsPutInt(Preference.acMantWsProxyPort.key, value)
        }

    var wsMantUseProxy: Boolean
        get() = sr.prefsGetBoolean(Preference.acMantWsUseProxy)
        set(value) {
            sr.prefsPutBoolean(Preference.acMantWsUseProxy.key, value)
        }

    var wsMantProxyUser: String
        get() = sr.prefsGetString(Preference.acMantWsProxyUser)
        set(value) {
            sr.prefsPutString(Preference.acMantWsProxyUser.key, value)
        }

    var wsMantProxyPass: String
        get() = sr.prefsGetString(Preference.acMantWsProxyPass)
        set(value) {
            sr.prefsPutString(Preference.acMantWsProxyPass.key, value)
        }

    var useImageControl: Boolean
        get() = sr.prefsGetBoolean(Preference.useImageControl)
        set(value) {
            sr.prefsPutBoolean(Preference.useImageControl.key, value)
        }

    var wsIcUrl: String
        get() = sr.prefsGetString(Preference.icWsServer)
        set(value) {
            sr.prefsPutString(Preference.icWsServer.key, value)
        }

    var wsIcNamespace: String
        get() = sr.prefsGetString(Preference.icWsNamespace)
        set(value) {
            sr.prefsPutString(Preference.icWsNamespace.key, value)
        }

    var wsIcProxy: String
        get() = sr.prefsGetString(Preference.icWsProxy)
        set(value) {
            sr.prefsPutString(Preference.icWsProxy.key, value)
        }

    var wsIcProxyPort: Int
        get() = sr.prefsGetInt(Preference.icWsProxyPort)
        set(value) {
            sr.prefsPutInt(Preference.icWsProxyPort.key, value)
        }

    var wsIcUseProxy: Boolean
        get() = sr.prefsGetBoolean(Preference.icWsUseProxy)
        set(value) {
            sr.prefsPutBoolean(Preference.icWsUseProxy.key, value)
        }

    var wsIcProxyUser: String
        get() = sr.prefsGetString(Preference.icWsProxyUser)
        set(value) {
            sr.prefsPutString(Preference.icWsProxyUser.key, value)
        }

    var wsIcProxyPass: String
        get() = sr.prefsGetString(Preference.icWsProxyPass)
        set(value) {
            sr.prefsPutString(Preference.icWsProxyPass.key, value)
        }

    var icUser: String
        get() = sr.prefsGetString(Preference.icUser)
        set(value) {
            sr.prefsPutString(Preference.icUser.key, value)
        }

    var icPass: String
        get() = sr.prefsGetString(Preference.icPass)
        set(value) {
            sr.prefsPutString(Preference.icPass.key, value)
        }

    var wsIcUser: String
        get() = sr.prefsGetString(Preference.icWsUser)
        set(value) {
            sr.prefsPutString(Preference.icWsUser.key, value)
        }

    var wsIcPass: String
        get() = sr.prefsGetString(Preference.icWsPass)
        set(value) {
            sr.prefsPutString(Preference.icWsPass.key, value)
        }

    var maxHeightOrWidth: Int
        get() = sr.prefsGetInt(Preference.icPhotoMaxHeightOrWidth)
        set(value) {
            sr.prefsPutInt(Preference.icPhotoMaxHeightOrWidth.key, value)
        }

    var allowScreenRotation: Boolean
        get() = sr.prefsGetBoolean(Preference.allowScreenRotation)
        set(value) {
            sr.prefsPutBoolean(Preference.allowScreenRotation.key, value)
        }

    // Estas propiedades se mantienen igual, ya que no usan sr
    var wsTestUrl: String = ""
    var wsTestNamespace: String = ""
    var wsTestProxyUrl: String = ""
    var wsTestProxyPort: Int = 0
    var wsTestUseProxy: Boolean = false
    var wsTestProxyUser: String = ""
    var wsTestProxyPass: String = ""
}