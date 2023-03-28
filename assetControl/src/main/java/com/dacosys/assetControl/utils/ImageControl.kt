package com.dacosys.assetControl.utils

import com.dacosys.assetControl.utils.settings.Preference

class ImageControl {
    companion object {
        fun closeImageControl() {
            com.dacosys.imageControl.Statics.cleanInstance()
        }

        fun setupImageControl() {
            // Setup ImageControl
            com.dacosys.imageControl.Statics.appAllowScreenRotation =
                Preferences.prefsGetBoolean(Preference.allowScreenRotation)

            val currentUser = Statics.currentUser()
            if (currentUser != null) {
                com.dacosys.imageControl.Statics.currentUserId = currentUser.userId
                com.dacosys.imageControl.Statics.currentUserName = currentUser.name
                com.dacosys.imageControl.Statics.newInstance()
            }

            com.dacosys.imageControl.Statics.useImageControl = Statics.useImageControl
            com.dacosys.imageControl.Statics.wsIcUrl = Statics.wsIcUrl
            com.dacosys.imageControl.Statics.wsIcNamespace = Statics.wsIcNamespace
            com.dacosys.imageControl.Statics.wsIcProxy = Statics.wsIcProxy
            com.dacosys.imageControl.Statics.wsIcProxyPort = Statics.wsIcProxyPort
            com.dacosys.imageControl.Statics.wsIcUseProxy = Statics.wsIcUseProxy
            com.dacosys.imageControl.Statics.wsIcProxyUser = Statics.wsIcProxyUser
            com.dacosys.imageControl.Statics.wsIcProxyPass = Statics.wsIcProxyPass
            com.dacosys.imageControl.Statics.icUser = Statics.icUser
            com.dacosys.imageControl.Statics.icPass = Statics.icPass
            com.dacosys.imageControl.Statics.wsIcUser = Statics.wsIcUser
            com.dacosys.imageControl.Statics.wsIcPass = Statics.wsIcPass
            com.dacosys.imageControl.Statics.maxHeightOrWidth = Statics.maxHeightOrWidth
        }
    }
}