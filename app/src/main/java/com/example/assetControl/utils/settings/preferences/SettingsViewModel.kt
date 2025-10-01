package com.example.assetControl.utils.settings.preferences

import androidx.lifecycle.ViewModel
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.utils.settings.config.Preference

class SettingsViewModel : ViewModel() {
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

    var wsMantNamespace: String
        get() = sr.prefsGetString(Preference.acMantWsNamespace)
        set(value) {
            sr.prefsPutString(Preference.acMantWsNamespace.key, value)
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

    var confPassword: String
        get() = sr.prefsGetString(Preference.confPassword)
        set(value) {
            sr.prefsPutString(Preference.confPassword.key, value)
        }

    var showConfButton: Boolean
        get() = sr.prefsGetBoolean(Preference.showConfButton)
        set(value) {
            sr.prefsPutBoolean(Preference.showConfButton.key, value)
        }

    var autoSend: Boolean
        get() = sr.prefsGetBoolean(Preference.autoSend)
        set(value) {
            sr.prefsPutBoolean(Preference.autoSend.key, value)
        }

    var collectorType: String
        get() = sr.prefsGetString(Preference.collectorType)
        set(value) {
            sr.prefsPutString(Preference.collectorType.key, value)
        }

    // region PRINTER
    var useBtPrinter: Boolean
        get() = sr.prefsGetBoolean(Preference.useBtPrinter)
        set(value) {
            sr.prefsPutBoolean(Preference.useBtPrinter.key, value)
        }

    var printerBtAddress: String
        get() = sr.prefsGetString(Preference.printerBtAddress)
        set(value) {
            sr.prefsPutString(Preference.printerBtAddress.key, value)
        }

    var defaultBarcodeLabelCustomAsset: Long
        get() = sr.prefsGetLong(Preference.defaultBarcodeLabelCustomAsset)
        set(value) {
            sr.prefsPutLong(Preference.defaultBarcodeLabelCustomAsset.key, value)
        }

    var defaultBarcodeLabelCustomWa: Long
        get() = sr.prefsGetLong(Preference.defaultBarcodeLabelCustomWa)
        set(value) {
            sr.prefsPutLong(Preference.defaultBarcodeLabelCustomWa.key, value)
        }

    var useNetPrinter: Boolean
        get() = sr.prefsGetBoolean(Preference.useNetPrinter)
        set(value) {
            sr.prefsPutBoolean(Preference.useNetPrinter.key, value)
        }

    var ipNetPrinter: String
        get() = sr.prefsGetString(Preference.ipNetPrinter)
        set(value) {
            sr.prefsPutString(Preference.ipNetPrinter.key, value)
        }

    var portNetPrinter: String
        get() = sr.prefsGetString(Preference.portNetPrinter)
        set(value) {
            sr.prefsPutString(Preference.portNetPrinter.key, value)
        }

    var lineSeparator: String
        get() = sr.prefsGetString(Preference.lineSeparator)
        set(value) {
            sr.prefsPutString(Preference.lineSeparator.key, value)
        }

    var printerPower: String
        get() = sr.prefsGetString(Preference.printerPower)
        set(value) {
            sr.prefsPutString(Preference.printerPower.key, value)
        }

    var printerSpeed: String
        get() = sr.prefsGetString(Preference.printerSpeed)
        set(value) {
            sr.prefsPutString(Preference.printerSpeed.key, value)
        }
//endregion PRINTER

    /* region Symbologies */
    var symbologyAztec: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyAztec)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyAztec.key, value)
        }

    var symbologyCODABAR: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyCODABAR)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyCODABAR.key, value)
        }

    var symbologyCode128: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyCode128)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyCode128.key, value)
        }

    var symbologyCode39: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyCode39)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyCode39.key, value)
        }

    var symbologyCode93: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyCode93)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyCode93.key, value)
        }

    var symbologyDataMatrix: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyDataMatrix)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyDataMatrix.key, value)
        }

    var symbologyEAN13: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyEAN13)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyEAN13.key, value)
        }

    var symbologyEAN8: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyEAN8)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyEAN8.key, value)
        }

    var symbologyITF: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyITF)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyITF.key, value)
        }

    var symbologyMaxiCode: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyMaxiCode)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyMaxiCode.key, value)
        }

    var symbologyPDF417: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyPDF417)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyPDF417.key, value)
        }

    var symbologyQRCode: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyQRCode)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyQRCode.key, value)
        }

    var symbologyRSS14: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyRSS14)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyRSS14.key, value)
        }

    var symbologyRSSExpanded: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyRSSExpanded)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyRSSExpanded.key, value)
        }

    var symbologyUPCA: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyUPCA)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyUPCA.key, value)
        }

    var symbologyUPCE: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyUPCE)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyUPCE.key, value)
        }

    var symbologyUPCEANExt: Boolean
        get() = sr.prefsGetBoolean(Preference.symbologyUPCEANExt)
        set(value) {
            sr.prefsPutBoolean(Preference.symbologyUPCEANExt.key, value)
        }
    /* endregion Symbologies */

    var useBtRfid: Boolean
        get() = sr.prefsGetBoolean(Preference.useBtRfid)
        set(value) {
            sr.prefsPutBoolean(Preference.useBtRfid.key, value)
        }

    var rfidBtAddress: String
        get() = sr.prefsGetString(Preference.rfidBtAddress)
        set(value) {
            sr.prefsPutString(Preference.rfidBtAddress.key, value)
        }

    var rfidBtName: String
        get() = sr.prefsGetString(Preference.rfidBtName)
        set(value) {
            sr.prefsPutString(Preference.rfidBtName.key, value)
        }

    var rfidReadPower: Int
        get() = sr.prefsGetInt(Preference.rfidReadPower)
        set(value) {
            sr.prefsPutInt(Preference.rfidReadPower.key, value)
        }

    var rfidWritePower: Int
        get() = sr.prefsGetInt(Preference.rfidWritePower)
        set(value) {
            sr.prefsPutInt(Preference.rfidWritePower.key, value)
        }

    var rfidSkipSameRead: Boolean
        get() = sr.prefsGetBoolean(Preference.rfidSkipSameRead)
        set(value) {
            sr.prefsPutBoolean(Preference.rfidSkipSameRead.key, value)
        }

    var rfidShockOnRead: Boolean
        get() = sr.prefsGetBoolean(Preference.rfidShockOnRead)
        set(value) {
            sr.prefsPutBoolean(Preference.rfidShockOnRead.key, value)
        }

    var rfidPlaySoundOnRead: Boolean
        get() = sr.prefsGetBoolean(Preference.rfidPlaySoundOnRead)
        set(value) {
            sr.prefsPutBoolean(Preference.rfidPlaySoundOnRead.key, value)
        }

    var rfidShowConnectedMessage: Boolean
        get() = sr.prefsGetBoolean(Preference.rfidShowConnectedMessage)
        set(value) {
            sr.prefsPutBoolean(Preference.rfidShowConnectedMessage.key, value)
        }

    var allowScreenRotation: Boolean
        get() = sr.prefsGetBoolean(Preference.allowScreenRotation)
        set(value) {
            sr.prefsPutBoolean(Preference.allowScreenRotation.key, value)
        }

    var sendBarcodeCheckDigit: Boolean
        get() = sr.prefsGetBoolean(Preference.sendBarcodeCheckDigit)
        set(value) {
            sr.prefsPutBoolean(Preference.sendBarcodeCheckDigit.key, value)
        }

    var flCameraPortraitLocX: Int
        get() = sr.prefsGetInt(Preference.flCameraPortraitLocX)
        set(value) {
            sr.prefsPutInt(Preference.flCameraPortraitLocX.key, value)
        }

    var flCameraPortraitLocY: Int
        get() = sr.prefsGetInt(Preference.flCameraPortraitLocY)
        set(value) {
            sr.prefsPutInt(Preference.flCameraPortraitLocY.key, value)
        }

    var flCameraPortraitWidth: Int
        get() = sr.prefsGetInt(Preference.flCameraPortraitWidth)
        set(value) {
            sr.prefsPutInt(Preference.flCameraPortraitWidth.key, value)
        }

    var flCameraPortraitHeight: Int
        get() = sr.prefsGetInt(Preference.flCameraPortraitHeight)
        set(value) {
            sr.prefsPutInt(Preference.flCameraPortraitHeight.key, value)
        }

    var flCameraLandscapeLocX: Int
        get() = sr.prefsGetInt(Preference.flCameraLandscapeLocX)
        set(value) {
            sr.prefsPutInt(Preference.flCameraLandscapeLocX.key, value)
        }

    var flCameraLandscapeLocY: Int
        get() = sr.prefsGetInt(Preference.flCameraLandscapeLocY)
        set(value) {
            sr.prefsPutInt(Preference.flCameraLandscapeLocY.key, value)
        }

    var flCameraLandscapeWidth: Int
        get() = sr.prefsGetInt(Preference.flCameraLandscapeWidth)
        set(value) {
            sr.prefsPutInt(Preference.flCameraLandscapeWidth.key, value)
        }

    var flCameraLandscapeHeight: Int
        get() = sr.prefsGetInt(Preference.flCameraLandscapeHeight)
        set(value) {
            sr.prefsPutInt(Preference.flCameraLandscapeHeight.key, value)
        }

    var flCameraContinuousMode: Boolean
        get() = sr.prefsGetBoolean(Preference.flCameraContinuousMode)
        set(value) {
            sr.prefsPutBoolean(Preference.flCameraContinuousMode.key, value)
        }

    var flCameraFilterRepeatedReads: Boolean
        get() = sr.prefsGetBoolean(Preference.flCameraFilterRepeatedReads)
        set(value) {
            sr.prefsPutBoolean(Preference.flCameraFilterRepeatedReads.key, value)
        }

    /* region AssetControl WebService */
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

    var acWsServer: String
        get() = sr.prefsGetString(Preference.acWsServer)
        set(value) {
            sr.prefsPutString(Preference.acWsServer.key, value)
        }

    var acWsNamespace: String
        get() = sr.prefsGetString(Preference.acWsNamespace)
        set(value) {
            sr.prefsPutString(Preference.acWsNamespace.key, value)
        }

    var acWsProxy: String
        get() = sr.prefsGetString(Preference.acWsProxy)
        set(value) {
            sr.prefsPutString(Preference.acWsProxy.key, value)
        }

    var acWsUseProxy: Boolean
        get() = sr.prefsGetBoolean(Preference.acWsUseProxy)
        set(value) {
            sr.prefsPutBoolean(Preference.acWsUseProxy.key, value)
        }

    var acWsProxyPort: Int
        get() = sr.prefsGetInt(Preference.acWsProxyPort)
        set(value) {
            sr.prefsPutInt(Preference.acWsProxyPort.key, value)
        }

    var acWsProxyUser: String
        get() = sr.prefsGetString(Preference.acWsProxyUser)
        set(value) {
            sr.prefsPutString(Preference.acWsProxyUser.key, value)
        }

    var acWsProxyPass: String
        get() = sr.prefsGetString(Preference.acWsProxyPass)
        set(value) {
            sr.prefsPutString(Preference.acWsProxyPass.key, value)
        }

    var acWsUser: String
        get() = sr.prefsGetString(Preference.acWsUser)
        set(value) {
            sr.prefsPutString(Preference.acWsUser.key, value)
        }

    var acWsPass: String
        get() = sr.prefsGetString(Preference.acWsPass)
        set(value) {
            sr.prefsPutString(Preference.acWsPass.key, value)
        }
    /* endregion AssetControl WebService */

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

    /* region AssetControl Maintenance WebService */
    var useAssetControlManteinance: Boolean
        get() = sr.prefsGetBoolean(Preference.useAssetControlManteinance)
        set(value) {
            sr.prefsPutBoolean(Preference.useAssetControlManteinance.key, value)
        }

    var acMantUser: String
        get() = sr.prefsGetString(Preference.acMantUser)
        set(value) {
            sr.prefsPutString(Preference.acMantUser.key, value)
        }

    var acMantPass: String
        get() = sr.prefsGetString(Preference.acMantPass)
        set(value) {
            sr.prefsPutString(Preference.acMantPass.key, value)
        }

    var acMantWsServer: String
        get() = sr.prefsGetString(Preference.acMantWsServer)
        set(value) {
            sr.prefsPutString(Preference.acMantWsServer.key, value)
        }

    var acMantWsNamespace: String
        get() = sr.prefsGetString(Preference.acMantWsNamespace)
        set(value) {
            sr.prefsPutString(Preference.acMantWsNamespace.key, value)
        }

    var acMantWsProxy: String
        get() = sr.prefsGetString(Preference.acMantWsProxy)
        set(value) {
            sr.prefsPutString(Preference.acMantWsProxy.key, value)
        }

    var acMantWsUseProxy: Boolean
        get() = sr.prefsGetBoolean(Preference.acMantWsUseProxy)
        set(value) {
            sr.prefsPutBoolean(Preference.acMantWsUseProxy.key, value)
        }

    var acMantWsProxyPort: Int
        get() = sr.prefsGetInt(Preference.acMantWsProxyPort)
        set(value) {
            sr.prefsPutInt(Preference.acMantWsProxyPort.key, value)
        }

    var acMantWsProxyUser: String
        get() = sr.prefsGetString(Preference.acMantWsProxyUser)
        set(value) {
            sr.prefsPutString(Preference.acMantWsProxyUser.key, value)
        }

    var acMantWsProxyPass: String
        get() = sr.prefsGetString(Preference.acMantWsProxyPass)
        set(value) {
            sr.prefsPutString(Preference.acMantWsProxyPass.key, value)
        }

    var acMantWsUser: String
        get() = sr.prefsGetString(Preference.acMantWsUser)
        set(value) {
            sr.prefsPutString(Preference.acMantWsUser.key, value)
        }

    var acMantWsPass: String
        get() = sr.prefsGetString(Preference.acMantWsPass)
        set(value) {
            sr.prefsPutString(Preference.acMantWsPass.key, value)
        }
    /* endregion AssetControl Maintenance WebService */

    /* region ImageControl WebService */
    var useImageControl: Boolean
        get() = sr.prefsGetBoolean(Preference.useImageControl)
        set(value) {
            sr.prefsPutBoolean(Preference.useImageControl.key, value)
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

    var icWsServer: String
        get() = sr.prefsGetString(Preference.icWsServer)
        set(value) {
            sr.prefsPutString(Preference.icWsServer.key, value)
        }

    var icWsNamespace: String
        get() = sr.prefsGetString(Preference.icWsNamespace)
        set(value) {
            sr.prefsPutString(Preference.icWsNamespace.key, value)
        }

    var icWsProxy: String
        get() = sr.prefsGetString(Preference.icWsProxy)
        set(value) {
            sr.prefsPutString(Preference.icWsProxy.key, value)
        }

    var icWsUseProxy: Boolean
        get() = sr.prefsGetBoolean(Preference.icWsUseProxy)
        set(value) {
            sr.prefsPutBoolean(Preference.icWsUseProxy.key, value)
        }

    var icWsProxyPort: Int
        get() = sr.prefsGetInt(Preference.icWsProxyPort)
        set(value) {
            sr.prefsPutInt(Preference.icWsProxyPort.key, value)
        }

    var icWsProxyUser: String
        get() = sr.prefsGetString(Preference.icWsProxyUser)
        set(value) {
            sr.prefsPutString(Preference.icWsProxyUser.key, value)
        }

    var icWsProxyPass: String
        get() = sr.prefsGetString(Preference.icWsProxyPass)
        set(value) {
            sr.prefsPutString(Preference.icWsProxyPass.key, value)
        }

    var icWsUser: String
        get() = sr.prefsGetString(Preference.icWsUser)
        set(value) {
            sr.prefsPutString(Preference.icWsUser.key, value)
        }

    var icWsPass: String
        get() = sr.prefsGetString(Preference.icWsPass)
        set(value) {
            sr.prefsPutString(Preference.icWsPass.key, value)
        }

    var icPhotoMaxHeightOrWidth: Int
        get() = sr.prefsGetInt(Preference.icPhotoMaxHeightOrWidth)
        set(value) {
            sr.prefsPutInt(Preference.icPhotoMaxHeightOrWidth.key, value)
        }
    /* endregion ImageControl WebService */

    var printLabelAssetOnlyActive: Boolean
        get() = sr.prefsGetBoolean(Preference.printLabelAssetOnlyActive)
        set(value) {
            sr.prefsPutBoolean(Preference.printLabelAssetOnlyActive.key, value)
        }

    var printLabelAssetShowImages: Boolean
        get() = sr.prefsGetBoolean(Preference.printLabelAssetShowImages)
        set(value) {
            sr.prefsPutBoolean(Preference.printLabelAssetShowImages.key, value)
        }

    var printLabelAssetShowCheckBoxes: Boolean
        get() = sr.prefsGetBoolean(Preference.printLabelAssetShowCheckBoxes)
        set(value) {
            sr.prefsPutBoolean(Preference.printLabelAssetShowCheckBoxes.key, value)
        }

    var reviewContentShowImages: Boolean
        get() = sr.prefsGetBoolean(Preference.reviewContentShowImages)
        set(value) {
            sr.prefsPutBoolean(Preference.reviewContentShowImages.key, value)
        }

    var reviewContentShowCheckBoxes: Boolean
        get() = sr.prefsGetBoolean(Preference.reviewContentShowCheckBoxes)
        set(value) {
            sr.prefsPutBoolean(Preference.reviewContentShowCheckBoxes.key, value)
        }

    var syncShowImages: Boolean
        get() = sr.prefsGetBoolean(Preference.syncShowImages)
        set(value) {
            sr.prefsPutBoolean(Preference.syncShowImages.key, value)
        }

    var showScannedCode: Boolean
        get() = sr.prefsGetBoolean(Preference.showScannedCode)
        set(value) {
            sr.prefsPutBoolean(Preference.showScannedCode.key, value)
        }

    var selectAssetOnlyActive: Boolean
        get() = sr.prefsGetBoolean(Preference.selectAssetOnlyActive)
        set(value) {
            sr.prefsPutBoolean(Preference.selectAssetOnlyActive.key, value)
        }

    var selectAssetMaintenanceOnlyActive: Boolean
        get() = sr.prefsGetBoolean(Preference.selectAssetMaintenanceOnlyActive)
        set(value) {
            sr.prefsPutBoolean(Preference.selectAssetMaintenanceOnlyActive.key, value)
        }

    var assetReviewAllowUnknownCodes: Boolean
        get() = sr.prefsGetBoolean(Preference.assetReviewAllowUnknownCodes)
        set(value) {
            sr.prefsPutBoolean(Preference.assetReviewAllowUnknownCodes.key, value)
        }

    var assetReviewAddUnknownAssets: Boolean
        get() = sr.prefsGetBoolean(Preference.assetReviewAddUnknownAssets)
        set(value) {
            sr.prefsPutBoolean(Preference.assetReviewAddUnknownAssets.key, value)
        }

    var assetReviewCompletedCheckBox: Boolean
        get() = sr.prefsGetBoolean(Preference.assetReviewCompletedCheckBox)
        set(value) {
            sr.prefsPutBoolean(Preference.assetReviewCompletedCheckBox.key, value)
        }

    var signReviewsAndMovements: Boolean
        get() = sr.prefsGetBoolean(Preference.signReviewsAndMovements)
        set(value) {
            sr.prefsPutBoolean(Preference.signReviewsAndMovements.key, value)
        }

    var quickReviews: Boolean
        get() = sr.prefsGetBoolean(Preference.quickReviews)
        set(value) {
            sr.prefsPutBoolean(Preference.quickReviews.key, value)
        }

    var selectDataCollectionRuleOnlyActive: Boolean
        get() = sr.prefsGetBoolean(Preference.selectDataCollectionRuleOnlyActive)
        set(value) {
            sr.prefsPutBoolean(Preference.selectDataCollectionRuleOnlyActive.key, value)
        }

    var registryError: Boolean
        get() = sr.prefsGetBoolean(Preference.registryError)
        set(value) {
            sr.prefsPutBoolean(Preference.registryError.key, value)
        }

    var selectItemCategoryOnlyActive: Boolean
        get() = sr.prefsGetBoolean(Preference.selectItemCategoryOnlyActive)
        set(value) {
            sr.prefsPutBoolean(Preference.selectItemCategoryOnlyActive.key, value)
        }

    var selectRouteOnlyActive: Boolean
        get() = sr.prefsGetBoolean(Preference.selectRouteOnlyActive)
        set(value) {
            sr.prefsPutBoolean(Preference.selectRouteOnlyActive.key, value)
        }

    var selectRouteDescription: String
        get() = sr.prefsGetString(Preference.selectRouteDescription)
        set(value) {
            sr.prefsPutString(Preference.selectRouteDescription.key, value)
        }

    var selectWarehouseOnlyActive: Boolean
        get() = sr.prefsGetBoolean(Preference.selectWarehouseOnlyActive)
        set(value) {
            sr.prefsPutBoolean(Preference.selectWarehouseOnlyActive.key, value)
        }

    var printLabelWarehouseAreaOnlyActive: Boolean
        get() = sr.prefsGetBoolean(Preference.printLabelWarehouseAreaOnlyActive)
        set(value) {
            sr.prefsPutBoolean(Preference.printLabelWarehouseAreaOnlyActive.key, value)
        }

    var selectWarehouseAreaOnlyActive: Boolean
        get() = sr.prefsGetBoolean(Preference.selectWarehouseAreaOnlyActive)
        set(value) {
            sr.prefsPutBoolean(Preference.selectWarehouseAreaOnlyActive.key, value)
        }

    var useNfc: Boolean
        get() = sr.prefsGetBoolean(Preference.useNfc)
        set(value) {
            sr.prefsPutBoolean(Preference.useNfc.key, value)
        }

    var acSyncInterval: Int
        get() = sr.prefsGetInt(Preference.acSyncInterval)
        set(value) {
            sr.prefsPutInt(Preference.acSyncInterval.key, value)
        }

    var acFilterRouteDescription: String
        get() = sr.prefsGetString(Preference.acFilterRouteDescription)
        set(value) {
            sr.prefsPutString(Preference.acFilterRouteDescription.key, value)
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