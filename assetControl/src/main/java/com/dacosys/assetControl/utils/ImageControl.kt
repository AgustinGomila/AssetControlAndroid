package com.dacosys.assetControl.utils

import com.dacosys.assetControl.utils.preferences.Preferences
import com.dacosys.assetControl.utils.preferences.Repository
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

            com.dacosys.imageControl.Statics.useImageControl = Repository.useImageControl
            com.dacosys.imageControl.Statics.wsIcUrl = Repository.wsIcUrl
            com.dacosys.imageControl.Statics.wsIcNamespace = Repository.wsIcNamespace
            com.dacosys.imageControl.Statics.wsIcProxy = Repository.wsIcProxy
            com.dacosys.imageControl.Statics.wsIcProxyPort = Repository.wsIcProxyPort
            com.dacosys.imageControl.Statics.wsIcUseProxy = Repository.wsIcUseProxy
            com.dacosys.imageControl.Statics.wsIcProxyUser = Repository.wsIcProxyUser
            com.dacosys.imageControl.Statics.wsIcProxyPass = Repository.wsIcProxyPass
            com.dacosys.imageControl.Statics.icUser = Repository.icUser
            com.dacosys.imageControl.Statics.icPass = Repository.icPass
            com.dacosys.imageControl.Statics.wsIcUser = Repository.wsIcUser
            com.dacosys.imageControl.Statics.wsIcPass = Repository.wsIcPass
            com.dacosys.imageControl.Statics.maxHeightOrWidth = Repository.maxHeightOrWidth
        }
    }
}