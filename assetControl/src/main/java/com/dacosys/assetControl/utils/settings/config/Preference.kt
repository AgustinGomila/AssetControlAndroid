package com.dacosys.assetControl.utils.settings.config

import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.enums.review.AssetReviewStatus
import com.dacosys.assetControl.network.sync.SyncRegistryType
import java.util.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Preference(key: String, defaultValue: Any, debugValue: Any) {
    var key: String = ""
    var defaultValue: Any? = null
    var debugValue: Any? = null

    init {
        this.key = key
        this.defaultValue = defaultValue
        this.debugValue = debugValue
    }

    override fun toString(): String {
        return key
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Preference) {
            false
        } else this.key == other.key
    }

    override fun hashCode(): Int {
        return this.key.hashCode()
    }

    companion object {
        var confPassword = Preference(
            "conf_password",
            "9876",
            "9876"
        )

        var showConfButton = Preference(
            "conf_general_show_conf_button",
            defaultValue = true,
            debugValue = true
        )

        var autoSend = Preference(
            "auto_send",
            defaultValue = false,
            debugValue = false
        )

        var collectorType = Preference(
            "collector_id",
            0,
            1
        )

        // region PRINTER

        var useBtPrinter = Preference(
            "conf_printer_use_bt_default",
            defaultValue = false,
            debugValue = false
        )

        var printerBtAddress = Preference(
            "printer_bt_address",
            "",
            ""
        )

        var defaultBarcodeLabelCustomAsset = Preference(
            "default_barcode_label_custom_asset",
            0L,
            0L
        )

        var defaultBarcodeLabelCustomWa = Preference(
            "default_barcode_label_custom_wa",
            0L,
            0L
        )

        var useNetPrinter = Preference(
            "conf_printer_use_net_default",
            defaultValue = false,
            debugValue = false
        )

        var ipNetPrinter = Preference(
            "printer_net_ip",
            defaultValue = "0.0.0.0",
            debugValue = "0.0.0.0"
        )

        var portNetPrinter = Preference(
            "printer_net_port",
            defaultValue = "9100",
            debugValue = "9100"
        )

        var lineSeparator = Preference(
            "line_separator",
            defaultValue = Char(10).toString(),
            debugValue = Char(10).toString()
        )

        var printerPower = Preference(
            "printer_power",
            defaultValue = "5",
            debugValue = "5"
        )

        var printerSpeed = Preference(
            "printer_speed",
            defaultValue = "1",
            debugValue = "1"
        )

        //endregion PRINTER

        /* region Symbologies */
        var symbologyAztec = Preference(
            "symbology_aztec",
            defaultValue = false,
            debugValue = false
        )

        var symbologyCODABAR = Preference(
            "symbology_codabar",
            defaultValue = false,
            debugValue = false
        )

        var symbologyCode128 = Preference(
            "symbology_code_128",
            defaultValue = true,
            debugValue = true
        )

        var symbologyCode39 = Preference(
            "symbology_code_39",
            defaultValue = true,
            debugValue = true
        )

        var symbologyCode93 = Preference(
            "symbology_code_93",
            defaultValue = false,
            debugValue = false
        )

        var symbologyDataMatrix = Preference(
            "symbology_data_matrix",
            defaultValue = true,
            debugValue = true
        )

        var symbologyEAN13 = Preference(
            "symbology_ean_13",
            defaultValue = true,
            debugValue = true
        )

        var symbologyEAN8 = Preference(
            "symbology_ean_8",
            defaultValue = true,
            debugValue = true
        )

        var symbologyITF = Preference(
            "symbology_itf",
            defaultValue = false,
            debugValue = false
        )

        var symbologyMaxiCode = Preference(
            "symbology_maxicode",
            defaultValue = false,
            debugValue = false
        )

        var symbologyPDF417 = Preference(
            "symbology_pdf417",
            defaultValue = false,
            debugValue = false
        )

        var symbologyQRCode = Preference(
            "symbology_qr_code",
            defaultValue = true,
            debugValue = true
        )

        var symbologyRSS14 = Preference(
            "symbology_rss_14",
            defaultValue = false,
            debugValue = false
        )

        var symbologyRSSExpanded = Preference(
            "symbology_rss_expanded",
            defaultValue = false,
            debugValue = false
        )

        var symbologyUPCA = Preference(
            "symbology_upc_a",
            defaultValue = false,
            debugValue = false
        )

        var symbologyUPCE = Preference(
            "symbology_upc_e",
            defaultValue = false,
            debugValue = false
        )

        var symbologyUPCEANExt = Preference(
            "symbology_upc_ean_extension",
            defaultValue = false,
            debugValue = false
        )
        /* endregion Symbologies */

        var useBtRfid = Preference(
            "conf_rfid_use_bt_default",
            defaultValue = false,
            debugValue = false
        )

        var rfidBtAddress = Preference(
            "rfid_bt_address",
            "",
            ""
        )

        var rfidBtName = Preference(
            "rfid_bluetooth_name",
            "",
            ""
        )

        var rfidReadPower = Preference(
            "rfid_read_power",
            26,
            26
        )

        var rfidWritePower = Preference(
            "rfid_write_power",
            10,
            10
        )

        var rfidSkipSameRead = Preference(
            "skip_same_on_rfid_read",
            defaultValue = false,
            debugValue = false
        )

        var rfidShockOnRead = Preference(
            "shock_on_rfid_read",
            defaultValue = false,
            debugValue = false
        )

        var rfidPlaySoundOnRead = Preference(
            "play_sound_on_rfid_read",
            defaultValue = false,
            debugValue = false
        )

        var rfidShowConnectedMessage = Preference(
            "show_rfid_connected_message",
            defaultValue = false,
            debugValue = false
        )

        var allowScreenRotation = Preference(
            "allow_screen_rotation",
            defaultValue = true,
            debugValue = true
        )

        var sendBarcodeCheckDigit = Preference(
            "conf_check_barcode_digit",
            defaultValue = true,
            debugValue = true
        )

        var flCameraPortraitLocX = Preference(
            "fl_camera_portrait_loc_x",
            defaultValue = 100,
            debugValue = 100
        )

        var flCameraPortraitLocY = Preference(
            "fl_camera_portrait_loc_y",
            defaultValue = 200,
            debugValue = 200
        )

        var flCameraPortraitWidth = Preference(
            "fl_camera_portrait_width",
            defaultValue = 600,
            debugValue = 600
        )

        var flCameraPortraitHeight = Preference(
            "fl_camera_portrait_height",
            defaultValue = 400,
            debugValue = 400
        )

        var flCameraLandscapeLocX = Preference(
            "fl_camera_landscape_loc_x",
            defaultValue = 100,
            debugValue = 100
        )

        var flCameraLandscapeLocY = Preference(
            "fl_camera_landscape_loc_y",
            defaultValue = 200,
            debugValue = 200
        )

        var flCameraLandscapeWidth = Preference(
            "fl_camera_landscape_width",
            defaultValue = 600,
            debugValue = 600
        )

        var flCameraLandscapeHeight = Preference(
            "fl_camera_landscape_height",
            defaultValue = 400,
            debugValue = 400
        )

        var flCameraContinuousMode = Preference(
            "fl_camera_continuous_mode",
            defaultValue = true,
            debugValue = true
        )

        var flCameraFilterRepeatedReads = Preference(
            "fl_camera_filter_repeated_reads",
            defaultValue = true,
            debugValue = true
        )

        /* region AssetControl WebService */
        var acUser = Preference(
            "ac_user",
            "",
            "test"
        )

        var acPass = Preference(
            "ac_pass",
            "",
            "pass"
        )

        var acWsServer = Preference(
            "ac_ws_server",
            "",
            "https://dev.dacosys.com/Milestone13/ac/s1/service.php"
        )

        var acWsNamespace = Preference(
            "ac_ws_namespace",
            "",
            "https://dev.dacosys.com/Milestone13/ac"
        )

        var acWsProxy = Preference(
            "ac_ws_proxy",
            "",
            ""
        )

        var acWsUseProxy = Preference(
            "ac_ws_use_proxy",
            defaultValue = false,
            debugValue = false
        )

        var acWsProxyPort = Preference(
            key = "ac_ws_proxy_port",
            defaultValue = 0,
            debugValue = 0
        )

        var acWsProxyUser = Preference(
            key = "ac_ws_proxy_user",
            defaultValue = "",
            debugValue = ""
        )

        var acWsProxyPass = Preference(
            key = "ac_ws_proxy_pass",
            defaultValue = "",
            debugValue = ""
        )

        var acWsUser = Preference(
            key = "ac_ws_user",
            defaultValue = "",
            debugValue = "dacosys"
        )

        var acWsPass = Preference(
            key = "ac_ws_pass",
            defaultValue = "",
            debugValue = "dacosys"
        )
        /* endregion AssetControl WebService */

        var urlPanel = Preference(
            key = "url_panel",
            defaultValue = "",
            debugValue = "DACOSYS"
        )

        var connectionTimeout = Preference(
            key = "connection_timeout",
            defaultValue = 25,
            debugValue = 25
        )

        var clientPackage = Preference(
            "client_package",
            "",
            ""
        )

        var installationCode = Preference(
            "installation_code",
            "",
            ""
        )

        var clientEmail = Preference(
            "client_email",
            "",
            ""
        )

        var clientPassword = Preference(
            "client_password",
            "",
            ""
        )

        /* region AssetControl Maintenance WebService */
        var useAssetControlManteinance = Preference(
            "use_asset_control_manteinance",
            defaultValue = false,
            debugValue = true
        )

        var acMantUser = Preference(
            "ac_mant_user",
            "",
            "test"
        )

        var acMantPass = Preference(
            "ac_mant_pass",
            "",
            "pass"
        )

        var acMantWsServer = Preference(
            "ac_mant_ws_server",
            "",
            "https://dev.dacosys.com/Milestone13/ac/smant/service.php"
        )

        var acMantWsNamespace = Preference(
            "ac_mant_ws_namespace",
            "",
            "https://dev.dacosys.com/Milestone13/ac"
        )

        var acMantWsProxy = Preference(
            "ac_mant_ws_proxy",
            "",
            ""
        )

        var acMantWsUseProxy = Preference(
            "ac_mant_ws_use_proxy",
            defaultValue = false,
            debugValue = false
        )

        var acMantWsProxyPort = Preference(
            "ac_mant_ws_proxy_port",
            0,
            0
        )

        var acMantWsProxyUser = Preference(
            "ac_mant_ws_proxy_user",
            "",
            ""
        )

        var acMantWsProxyPass = Preference(
            "ac_mant_ws_proxy_pass",
            "",
            ""
        )

        var acMantWsUser = Preference(
            "ac_mant_ws_user",
            "",
            "dacosys"
        )

        var acMantWsPass = Preference(
            "ac_mant_ws_pass",
            "",
            "dacosys"
        )
        /* endregion AssetControl Maintenance WebService */

        /* region ImageControl WebService */
        var useImageControl = Preference(
            "use_image_control",
            defaultValue = false,
            debugValue = true
        )

        var icUser = Preference(
            "ic_user",
            "",
            "test"
        )

        var icPass = Preference(
            "ic_pass",
            "",
            "pass"
        )

        var icWsServer = Preference(
            "ic_ws_server",
            "",
            "https://dev.dacosys.com/Milestone13/ic/s1/service.php"
        )

        var icWsNamespace = Preference(
            "ic_ws_namespace",
            "",
            "https://dev.dacosys.com/ic"
        )

        var icWsProxy = Preference(
            "ic_ws_proxy",
            "",
            ""
        )

        var icWsUseProxy = Preference(
            "ic_ws_use_proxy",
            defaultValue = false,
            debugValue = false
        )

        var icWsProxyPort = Preference(
            "ic_ws_proxy_port",
            0,
            0
        )

        var icWsProxyUser = Preference(
            "ic_ws_proxy_user",
            "",
            ""
        )

        var icWsProxyPass = Preference(
            "ic_ws_proxy_pass",
            "",
            ""
        )

        var icWsUser = Preference(
            "ic_ws_user",
            "",
            ""
        )

        var icWsPass = Preference(
            "ic_ws_pass",
            "",
            ""
        )

        var icPhotoMaxHeightOrWidth = Preference(
            "ic_photo_max_height_or_width",
            1280,
            1280
        )
        /* endregion ImageControl WebService */

        var printLabelAssetOnlyActive = Preference(
            "asset_print_only_active",
            defaultValue = true,
            debugValue = true
        )

        var printLabelAssetShowImages = Preference(
            "asset_print_show_images",
            defaultValue = false,
            debugValue = false
        )

        var printLabelAssetShowCheckBoxes = Preference(
            "asset_print_show_check_boxes",
            defaultValue = false,
            debugValue = false
        )

        var reviewContentShowImages = Preference(
            "review_content_show_images",
            defaultValue = false,
            debugValue = false
        )

        var reviewContentShowCheckBoxes = Preference(
            "review_content_show_check_boxes",
            defaultValue = false,
            debugValue = false
        )

        var syncShowImages = Preference(
            "asset_print_show_images",
            defaultValue = false,
            debugValue = false
        )

        var showScannedCode = Preference(
            "show_scanned_code",
            defaultValue = false,
            debugValue = false
        )

        var selectAssetOnlyActive = Preference(
            "asset_select_only_active",
            defaultValue = true,
            debugValue = true
        )

        var selectAssetMaintenanceOnlyActive = Preference(
            "asset_maintenance_select_only_active",
            defaultValue = true,
            debugValue = true
        )

        var assetReviewAllowUnknownCodes = Preference(
            "asset_review_allow_unknown_codes",
            defaultValue = false,
            debugValue = false
        )

        var assetReviewAddUnknownAssets = Preference(
            "asset_review_add_unknown_assets",
            defaultValue = false,
            debugValue = false
        )

        var assetReviewCompletedCheckBox = Preference(
            "asset_review_completed_checkbox",
            defaultValue = false,
            debugValue = false
        )

        var signReviewsAndMovements = Preference(
            "conf_general_sign_rev_mov",
            defaultValue = false,
            debugValue = false
        )

        var quickReviews = Preference(
            "conf_general_quick_review",
            defaultValue = false,
            debugValue = false
        )

        var selectDataCollectionRuleOnlyActive = Preference(
            "data_collection_rule_select_only_active",
            defaultValue = true,
            debugValue = true
        )

        var registryError = Preference(
            "conf_general_registry_error",
            defaultValue = false,
            debugValue = false
        )

        var selectItemCategoryOnlyActive = Preference(
            "item_category_select_only_active",
            defaultValue = true,
            debugValue = true
        )

        var selectRouteOnlyActive = Preference(
            "route_select_only_active",
            defaultValue = true,
            debugValue = true
        )

        var selectRouteDescription = Preference(
            "route_select_description",
            defaultValue = "",
            debugValue = ""
        )

        var selectWarehouseOnlyActive = Preference(
            "warehouse_select_only_active",
            defaultValue = true,
            debugValue = true
        )

        var printLabelWarehouseAreaOnlyActive = Preference(
            "wa_print_only_active",
            defaultValue = true,
            debugValue = true
        )

        var selectWarehouseAreaOnlyActive = Preference(
            "warehouse_area_select_only_active",
            defaultValue = true,
            debugValue = true
        )

        var assetReviewVisibleStatus = Preference(
            "asset_review_visible_status",
            defaultValue = ArrayList(AssetReviewStatus.getAll().map { it.id.toString() }),
            debugValue = ArrayList(AssetReviewStatus.getAll().map { it.id.toString() })
        )

        var assetReviewContentVisibleStatus = Preference(
            "asset_review_content_visible_status",
            defaultValue = ArrayList(AssetReviewStatus.getAll().map { it.id.toString() }),
            debugValue = ArrayList(AssetReviewStatus.getAll().map { it.id.toString() })
        )

        var assetSelectFragmentVisibleStatus = Preference(
            "asset_select_fragment_visible_status",
            defaultValue = ArrayList(AssetStatus.getAll().map { it.id.toString() }),
            debugValue = ArrayList(AssetStatus.getAll().map { it.id.toString() })
        )

        var useNfc = Preference(
            "conf_nfc_use_default",
            defaultValue = false,
            debugValue = false
        )

        var acSyncInterval = Preference(
            "ac_sync_interval",
            defaultValue = 300,
            debugValue = 300
        )

        var acFilterRouteDescription = Preference(
            "filter_route_description",
            "",
            ""
        )

        var syncVisibleRegistry = Preference(
            "sync_visible_registry",
            defaultValue = SyncRegistryType.getSyncAsString(),
            debugValue = SyncRegistryType.getSyncAsString()
        )


        fun getAcMant(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                useAssetControlManteinance,
                acMantPass,
                acMantUser,
                acMantWsNamespace,
                acMantWsPass,
                acMantWsProxy,
                acMantWsProxyPort,
                acMantWsServer,
                acMantWsUseProxy,
                acMantWsProxyUser,
                acMantWsProxyPass,
                acMantWsUser
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getAcWebserivce(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                acPass,
                acUser,
                acWsNamespace,
                acWsPass,
                acWsProxy,
                acWsProxyPort,
                acWsServer,
                acWsUseProxy,
                acWsProxyUser,
                acWsProxyPass,
                acWsUser
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getRfidNfc(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                useNfc,
                useBtRfid,
                rfidBtAddress,
                rfidBtName,
                rfidPlaySoundOnRead,
                rfidReadPower,
                rfidShockOnRead,
                rfidSkipSameRead,
                rfidWritePower,
                rfidShowConnectedMessage,

                flCameraPortraitLocX,
                flCameraPortraitLocY,
                flCameraPortraitWidth,
                flCameraPortraitHeight,
                flCameraLandscapeLocX,
                flCameraLandscapeLocY,
                flCameraLandscapeWidth,
                flCameraLandscapeHeight,
                flCameraContinuousMode,
                flCameraFilterRepeatedReads
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getPersistentStateActivity(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                assetReviewAddUnknownAssets,
                assetReviewAllowUnknownCodes,
                assetReviewCompletedCheckBox,
                assetReviewVisibleStatus,
                assetReviewContentVisibleStatus,
                assetSelectFragmentVisibleStatus,
                printLabelAssetOnlyActive,
                printLabelAssetShowImages,
                printLabelAssetShowCheckBoxes,
                printLabelWarehouseAreaOnlyActive,
                reviewContentShowImages,
                reviewContentShowCheckBoxes,
                selectAssetMaintenanceOnlyActive,
                selectAssetOnlyActive,
                selectDataCollectionRuleOnlyActive,
                selectItemCategoryOnlyActive,
                selectRouteOnlyActive,
                selectRouteDescription,
                selectWarehouseAreaOnlyActive,
                selectWarehouseOnlyActive,
                syncShowImages
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getPrinter(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                useBtPrinter,
                printerBtAddress,
                useNetPrinter,
                ipNetPrinter,
                portNetPrinter,
                printerPower,
                printerSpeed,
                defaultBarcodeLabelCustomAsset,
                defaultBarcodeLabelCustomWa
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getSymbology(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                symbologyPDF417,
                symbologyAztec,
                symbologyQRCode,
                symbologyCODABAR,
                symbologyCode128,
                symbologyCode39,
                symbologyCode93,
                symbologyDataMatrix,
                symbologyEAN13,
                symbologyEAN8,
                symbologyMaxiCode,
                symbologyRSS14,
                symbologyRSSExpanded,
                symbologyUPCA,
                symbologyUPCE
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getClientPackage(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                urlPanel,
                clientEmail,
                clientPassword,
                installationCode,
                clientPackage
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getClient(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                clientEmail,
                clientPassword,
                installationCode
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getAppConf(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                autoSend,
                connectionTimeout,
                collectorType,
                confPassword,
                quickReviews,
                registryError,
                showConfButton,
                signReviewsAndMovements,
                acSyncInterval,
                acFilterRouteDescription,
                allowScreenRotation,
                sendBarcodeCheckDigit,
                showScannedCode
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getImageControl(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            Collections.addAll(
                allSections,
                useImageControl,
                icPass,
                icUser,
                icWsNamespace,
                icWsPass,
                icWsProxy,
                icWsProxyPort,
                icWsProxyUser,
                icWsProxyPass,
                icWsServer,
                icWsUseProxy,
                icWsUser,
                icPhotoMaxHeightOrWidth
            )

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getEnviroment(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()

            allSections.addAll(getAcMant())
            allSections.addAll(getAcWebserivce())
            allSections.addAll(getClientPackage())
            allSections.addAll(getImageControl())

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        private fun getAll(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()

            allSections.addAll(getAcMant())
            allSections.addAll(getAcWebserivce())
            allSections.addAll(getAppConf())
            allSections.addAll(getClientPackage())
            allSections.addAll(getImageControl())
            allSections.addAll(getPersistentStateActivity())
            allSections.addAll(getPrinter())
            allSections.addAll(getRfidNfc())
            allSections.addAll(getSymbology())

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getConfigPreferences(): ArrayList<Preference> {
            val allSections = ArrayList<Preference>()
            allSections.addAll(getAppConf())
            allSections.addAll(getImageControl())
            allSections.addAll(getAcWebserivce())

            return ArrayList(allSections.sortedWith(compareBy { it.key }))
        }

        fun getByKey(key: String): Preference? {
            return getAll().firstOrNull { it.key == key }
        }
    }
}