package com.dacosys.assetControl.utils.settings.preferences

import com.dacosys.assetControl.utils.settings.config.Preference

class Repository {
    companion object {
        val clientEmail: String
            get() {
                return Preferences.prefsGetString(Preference.clientEmail)
            }

        val clientPassword: String
            get() {
                return Preferences.prefsGetString(Preference.clientPassword)
            }

        val clientPackage: String
            get() {
                return Preferences.prefsGetString(Preference.clientPackage)
            }

        val installationCode: String
            get() {
                return Preferences.prefsGetString(Preference.installationCode)
            }

        val wsUrl: String //"https://dev.dacosys.com/Milestone13/ac/s1/service.php"
            get() {
                return Preferences.prefsGetString(Preference.acWsServer)
            }

        val wsNamespace: String //"https://dev.dacosys.com/Milestone13/ac/s1"
            get() {
                return Preferences.prefsGetString(Preference.acWsNamespace)
            }

        val urlPanel: String //"http://client.dacosys.com:80/7RDRHHAH/panel"
            get() {
                return Preferences.prefsGetString(Preference.urlPanel)
            }

        val connectionTimeout: Int
            get() {
                return Preferences.prefsGetInt(Preference.connectionTimeout)
            }

        val wsProxy: String
            get() {
                return Preferences.prefsGetString(Preference.acWsProxy)
            }

        val wsProxyPort: Int
            get() {
                return Preferences.prefsGetInt(Preference.acWsProxyPort)
            }

        val wsUseProxy: Boolean
            get() {
                return Preferences.prefsGetBoolean(Preference.acWsUseProxy)
            }

        val wsProxyUser: String
            get() {
                return Preferences.prefsGetString(Preference.acWsProxyUser)
            }

        val wsProxyPass: String
            get() {
                return Preferences.prefsGetString(Preference.acWsProxyPass)
            }

        val wsUrlCron: String //"https://dev.dacosys.com/Milestone13/ac"
            get() {
                return wsNamespace.replace("/s1", "")
            }

        val wsMantUrl: String //"https://dev.dacosys.com/Milestone13/ac/smant/service.php"
            get() {
                return Preferences.prefsGetString(Preference.acMantWsServer)
            }

        val wsMantNamespace: String //"https://dev.dacosys.com/Milestone13/ac/smant"
            get() {
                return Preferences.prefsGetString(Preference.acMantWsNamespace)
            }

        val wsMantProxy: String
            get() {
                return Preferences.prefsGetString(Preference.acMantWsProxy)
            }

        val wsMantProxyPort: Int
            get() {
                return Preferences.prefsGetInt(Preference.acMantWsProxyPort)
            }

        val wsMantUseProxy: Boolean
            get() {
                return Preferences.prefsGetBoolean(Preference.acMantWsUseProxy)
            }

        val wsMantProxyUser: String
            get() {
                return Preferences.prefsGetString(Preference.acMantWsProxyUser)
            }

        val wsMantProxyPass: String
            get() {
                return Preferences.prefsGetString(Preference.acMantWsProxyPass)
            }

        val useImageControl: Boolean
            get() {
                return Preferences.prefsGetBoolean(Preference.useImageControl)
            }

        val wsIcUrl: String //"https://dev.dacosys.com/Milestone13/ic/s1/service.php"
            get() {
                return Preferences.prefsGetString(Preference.icWsServer)
            }

        val wsIcNamespace: String  //"https://dev.dacosys.com/Milestone13/ic/s1"
            get() {
                return Preferences.prefsGetString(Preference.icWsNamespace)
            }

        val wsIcProxy: String
            get() {
                return Preferences.prefsGetString(Preference.icWsProxy)
            }

        val wsIcProxyPort: Int
            get() {
                return Preferences.prefsGetInt(Preference.icWsProxyPort)
            }

        val wsIcUseProxy: Boolean
            get() {
                return Preferences.prefsGetBoolean(Preference.icWsUseProxy)
            }

        val wsIcProxyUser: String
            get() {
                return Preferences.prefsGetString(Preference.icWsProxyUser)
            }

        val wsIcProxyPass: String
            get() {
                return Preferences.prefsGetString(Preference.icWsProxyPass)
            }

        val icUser: String
            get() {
                return Preferences.prefsGetString(Preference.icUser)
            }

        val icPass: String
            get() {
                return Preferences.prefsGetString(Preference.icPass)
            }

        val wsIcUser: String
            get() {
                return Preferences.prefsGetString(Preference.icWsUser)
            }

        val wsIcPass: String
            get() {
                return Preferences.prefsGetString(Preference.icWsPass)
            }

        val maxHeightOrWidth: Int
            get() {
                return Preferences.prefsGetInt(Preference.icPhotoMaxHeightOrWidth)
            }

        var wsTestUrl: String = ""
        var wsTestNamespace: String = ""
        var wsTestProxyUrl: String = ""
        var wsTestProxyPort: Int = 0
        var wsTestUseProxy: Boolean = false
        var wsTestProxyUser: String = ""
        var wsTestProxyPass: String = ""
    }
}