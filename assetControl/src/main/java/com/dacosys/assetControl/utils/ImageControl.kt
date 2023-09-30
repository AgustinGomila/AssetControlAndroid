package com.dacosys.assetControl.utils

import com.dacosys.assetControl.utils.preferences.Preferences
import com.dacosys.assetControl.utils.preferences.Repository
import com.dacosys.assetControl.utils.settings.Preference

class ImageControl {

    companion object {

        lateinit var imageControl: com.dacosys.imageControl.ImageControl

        fun closeImageControl() {
            imageControl.cleanInstance()
        }

        fun setupImageControl() {
            imageControl.cleanInstance()

            imageControl.appAllowScreenRotation =
                Preferences.prefsGetBoolean(Preference.allowScreenRotation)

            val currentUser = Statics.currentUser()
            if (currentUser != null) {
                imageControl.userId = currentUser.userId
                imageControl.userName = currentUser.name
            }

            imageControl.useImageControl = Repository.useImageControl
            imageControl.wsIcUrl = Repository.wsIcUrl
            imageControl.wsIcNamespace = Repository.wsIcNamespace
            imageControl.wsIcProxy = Repository.wsIcProxy
            imageControl.wsIcProxyPort = Repository.wsIcProxyPort
            imageControl.wsIcUseProxy = Repository.wsIcUseProxy
            imageControl.wsIcProxyUser = Repository.wsIcProxyUser
            imageControl.wsIcProxyPass = Repository.wsIcProxyPass
            imageControl.icUser = Repository.icUser
            imageControl.icPass = Repository.icPass
            imageControl.wsIcUser = Repository.wsIcUser
            imageControl.wsIcPass = Repository.wsIcPass
            imageControl.maxHeightOrWidth = Repository.maxHeightOrWidth
            imageControl.connectionTimeout = Repository.connectionTimeout

        }
    }
}