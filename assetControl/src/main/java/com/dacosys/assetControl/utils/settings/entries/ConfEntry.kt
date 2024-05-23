package com.dacosys.assetControl.utils.settings.entries

import com.dacosys.assetControl.utils.Statics
import java.lang.reflect.Type
import java.util.*

/**
 * Created by Agustin on 16/01/2017.
 */

class ConfEntry(
    var confEntryId: Long,
    var description: String,
    var confSection: ConfSection,
    var valueType: Type,
    var defaultValue: Any,
) {
    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is ConfEntry) {
            false
        } else this.confEntryId == other.confEntryId
    }

    override fun hashCode(): Int {
        return this.confEntryId.hashCode()
    }

    class CustomComparator : Comparator<ConfEntry> {
        override fun compare(o1: ConfEntry, o2: ConfEntry): Int {
            if (o1.confEntryId < o2.confEntryId) {
                return -1
            } else if (o1.confEntryId > o2.confEntryId) {
                return 1
            }
            return 0
        }
    }

    companion object {
        //<editor-fold desc="100 - Application Data"
        private var appShowExitButton = ConfEntry(
            100, "ShowExitButton", ConfSection.appData,
            Boolean::class.javaObjectType, true
        )

        private var appShowConfigurationButton = ConfEntry(
            101, "ShowConfigurationButton", ConfSection.appData,
            Boolean::class.javaObjectType, true
        )

        private var appLogFile = ConfEntry(
            102, "LogFile", ConfSection.appData,
            Boolean::class.javaObjectType, false
        )

        private var appShowDownloadDBButton = ConfEntry(
            103, "ShowDownloadDBButton", ConfSection.appData,
            Boolean::class.javaObjectType, true
        )

        private var appConfigurationPassword = ConfEntry(
            104, "ConfigurationPassword", ConfSection.appData,
            String::class.javaObjectType, "9876"
        )

        private var appHandSignature = ConfEntry(
            105, "HandSignature", ConfSection.appData,
            Boolean::class.javaObjectType, false
        )

        private var appUseFreeSuggestedLocation = ConfEntry(
            106, "UseFreeSuggestedLocation", ConfSection.appData,
            Boolean::class.javaObjectType, false
        )

        private var appShowFullLocation = ConfEntry(
            107, "ShowFullLocation", ConfSection.appData,
            Boolean::class.javaObjectType, false
        )

        private var appMissingAssetsDestWarehouseArea = ConfEntry(
            108, "MissingAssetsDestWarehouseArea", ConfSection.appData,
            Int::class.javaObjectType, 0
        )

        //</editor-fold>

        //<editor-fold desc="200 - Collector Data"
        private var collCollectorType = ConfEntry(
            200, "CollectorType", ConfSection.collData,
            Int::class.javaObjectType, 4
        )

        private var collRfidInstalled = ConfEntry(
            201, "RFIDInstalled", ConfSection.collData,
            Boolean::class.javaObjectType, false
        )

        private var collRfidPort = ConfEntry(
            202, "RFIDPort", ConfSection.collData,
            Int::class.javaObjectType, -1
        )

        var collCollectorScanner2DEnabled = ConfEntry(
            203, "CollectorScanner2DEnabled", ConfSection.collData,
            Boolean::class.javaObjectType, true
        )

        var collCollectorScannerSendCheckDigit = ConfEntry(
            204, "CollectorScannerSendCheckDigit", ConfSection.collData,
            Boolean::class.javaObjectType, true
        )

        private var collRFIDInventoryMode = ConfEntry(
            205, "Inventory Mode", ConfSection.collData,
            Boolean::class.javaObjectType, true
        )

        private var collRFIDFieldStrengthDb = ConfEntry(
            206, "Field Strength dB", ConfSection.collData,
            Int::class.javaObjectType, 25
        )

        //</editor-fold>

        //<editor-fold desc="300 - WebService Data"
        private var wsUser = ConfEntry(
            300, "User", ConfSection.webserviceData,
            String::class.javaObjectType, ""
        )

        private var wsPass = ConfEntry(
            301, "Pass", ConfSection.webserviceData,
            String::class.javaObjectType, ""
        )

        private var wsDB = ConfEntry(
            302, "DB", ConfSection.webserviceData,
            String::class.javaObjectType, ""
        )

        private var wsUrl = ConfEntry(
            303, "Server", ConfSection.webserviceData,
            String::class.javaObjectType, ""
        )

        private var wsPort = ConfEntry(
            304, "Port", ConfSection.webserviceData,
            Int::class.javaObjectType, 0
        )

        private var wsConnectionLifetime = ConfEntry(
            305, "ConnectionLifetime", ConfSection.webserviceData,
            Int::class.javaObjectType, 100
        )

        private var wsConnectTimeout = ConfEntry(
            306, "ConnectTimeout", ConfSection.webserviceData,
            Int::class.javaObjectType, 10
        )

        private var wsDefaultCommandTimeout = ConfEntry(
            307, "DefaultCommandTimeout", ConfSection.webserviceData,
            Int::class.javaObjectType, 10
        )

        private var wsMaxReceivedMessageSize = ConfEntry(
            308, "MaxReceivedMessageSize", ConfSection.webserviceData,
            Int::class.javaObjectType, 64
        )

        private var wsMaxBufferSize = ConfEntry(
            309, "MaxBufferSize", ConfSection.webserviceData,
            Int::class.javaObjectType, 64
        )

        private var wsSkipPing = ConfEntry(
            310, "SkipPing", ConfSection.webserviceData,
            Boolean::class.javaObjectType, false
        )

        private var wsDateTimeFormat = ConfEntry(
            311, "DateTimeFormat", ConfSection.webserviceData,
            String::class.javaObjectType, "yyyy-MM-dd HH:mm:ss"
        )

        //</editor-fold>

        //<editor-fold desc="400 - Mysql Data"

        private var myUser = ConfEntry(
            400, "User", ConfSection.mySqlConnectionData,
            String::class.javaObjectType, ""
        )

        private var myPass = ConfEntry(
            401, "Pass", ConfSection.mySqlConnectionData,
            String::class.javaObjectType, ""
        )

        private var myDB = ConfEntry(
            402, "DB", ConfSection.mySqlConnectionData,
            String::class.javaObjectType, ""
        )

        private var myServer = ConfEntry(
            403, "Server", ConfSection.mySqlConnectionData,
            String::class.javaObjectType, ""
        )

        private var myPort = ConfEntry(
            404, "Port", ConfSection.mySqlConnectionData,
            Int::class.javaObjectType, 0
        )

        private var myConnectionLifetime = ConfEntry(
            405, "ConnectionLifetime", ConfSection.mySqlConnectionData,
            Int::class.javaObjectType, 100
        )

        private var myConnectTimeout = ConfEntry(
            406, "ConnectTimeout", ConfSection.mySqlConnectionData,
            Int::class.javaObjectType, 25
        )

        private var myDefaultCommandTimeout = ConfEntry(
            407, "DefaultCommandTimeout", ConfSection.mySqlConnectionData,
            Int::class.javaObjectType, 25
        )

        private var myDBConf = ConfEntry(
            408, "DBConf", ConfSection.mySqlConnectionData,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="500 - WebService Proxy Data"

        private var wsProxyUse = ConfEntry(
            500, "ProxyUse", ConfSection.proxyWebServiceData,
            Boolean::class.javaObjectType, false
        )

        private var wsProxyUseDefaultProxy = ConfEntry(
            501, "UseDefaultProxy", ConfSection.proxyWebServiceData,
            Boolean::class.javaObjectType, true
        )

        private var wsProxyUrl = ConfEntry(
            502, "ProxyUrl", ConfSection.proxyWebServiceData,
            String::class.javaObjectType, ""
        )

        private var wsProxyPort = ConfEntry(
            503, "ProxyPort", ConfSection.proxyWebServiceData,
            Int::class.javaObjectType, 80
        )

        private var wsProxyUseCredentials = ConfEntry(
            504, "ProxyUseCredentials", ConfSection.proxyWebServiceData,
            Boolean::class.javaObjectType, false
        )

        private var wsProxyUseDefaultCredentials = ConfEntry(
            505, "ProxyUseDefaultCredentials", ConfSection.proxyWebServiceData,
            Boolean::class.javaObjectType, true
        )

        private var wsProxyUser = ConfEntry(
            506, "ProxyUser", ConfSection.proxyWebServiceData,
            String::class.javaObjectType, ""
        )

        private var wsProxyPass = ConfEntry(
            507, "ProxyPass", ConfSection.proxyWebServiceData,
            String::class.javaObjectType, ""
        )

        private var wsProxyDomain = ConfEntry(
            508, "ProxyDomain", ConfSection.proxyWebServiceData,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="600 - MySql Proxy Data"

        //var myProxyUse = ConfEntry(
        //    600, "ProxyUse", ConfSection.proxyMySqlData,
        //   Boolean::class.javaObjectType, false)

        //var myProxyUseDefaultProxy = ConfEntry(
        //    601, "UseDefaultProxy", ConfSection.proxyMySqlData,
        //   Boolean::class.javaObjectType, true)

        //var myProxyUrl = ConfEntry(
        //    602, "ProxyUrl", ConfSection.proxyMySqlData,
        //    String.String::class.javaObjectType, "")

        //var myProxyPort = ConfEntry(
        //    603, "ProxyPort", ConfSection.proxyMySqlData,
        //    Int.Int::class.javaObjectType, 80)

        //var myProxyUseCredentials = ConfEntry(
        //    604, "ProxyUseCredentials", ConfSection.proxyMySqlData,
        //   Boolean::class.javaObjectType, false)

        //var myProxyUseDefaultCredentials = ConfEntry(
        //    605, "ProxyUseDefaultCredentials", ConfSection.proxyMySqlData,
        //   Boolean::class.javaObjectType, true)

        //var myProxyUser = ConfEntry(
        //    606, "ProxyUser", ConfSection.proxyMySqlData,
        //    String.String::class.javaObjectType, "")

        //var myProxyPass = ConfEntry(
        //    607, "ProxyPass", ConfSection.proxyMySqlData,
        //    String.String::class.javaObjectType, "")

        //var myProxyDomain = ConfEntry(
        //    608, "ProxyDomain", ConfSection.proxyMySqlData,
        //    String.String::class.javaObjectType, "")

        //</editor-fold>

        //<editor-fold desc="700 - License Data"

        private var liCodeA = ConfEntry(
            700, "CodeA", ConfSection.licenseData,
            String::class.javaObjectType, ""
        )

        private var liCodeB = ConfEntry(
            701, "CodeB", ConfSection.licenseData,
            String::class.javaObjectType, ""
        )

        private var liCodeAN = ConfEntry(
            702, "CodeAN", ConfSection.licenseData,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="800 - WarehouseCounter Data"

        private var wcLastUpdate = ConfEntry(
            800, "LastUpdate", ConfSection.warehouseCounterData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var wcRadConfClient = ConfEntry(
            801, "RadConfClient", ConfSection.warehouseCounterData,
            String::class.javaObjectType, ""
        )

        private var wcRadConfItem = ConfEntry(
            802, "RadConfItem", ConfSection.warehouseCounterData,
            String::class.javaObjectType, ""
        )

        private var wcRadConfSalesman = ConfEntry(
            803, "RadConfSalesman", ConfSection.warehouseCounterData,
            String::class.javaObjectType, ""
        )

        private var wcRadConfClientSelect = ConfEntry(
            804, "RadConfClientSelect", ConfSection.warehouseCounterData,
            String::class.javaObjectType, ""
        )

        private var wcRadConfItemSelect = ConfEntry(
            805, "RadConfItemSelect", ConfSection.warehouseCounterData,
            String::class.javaObjectType, ""
        )

        private var wcRadConfSalesmanSelect = ConfEntry(
            806, "RadConfSalesmanSelect", ConfSection.warehouseCounterData,
            String::class.javaObjectType, ""
        )

        private var wcRadConfSalesmanSelectReport = ConfEntry(
            807, "RadConfSalesmanSelectReport", ConfSection.warehouseCounterData,
            String::class.javaObjectType, ""
        )

        private var wcDaysToShow = ConfEntry(
            808, "DaysToShow", ConfSection.warehouseCounterData,
            String::class.javaObjectType, "10"
        )

        private var wcSoftwareConfigured = ConfEntry(
            809, "SoftwareConfigured", ConfSection.warehouseCounterData,
            Boolean::class.javaObjectType, false
        )

        private var wcDeleteOrderAfterDownload = ConfEntry(
            810, "DeleteOrderAfterDownload", ConfSection.warehouseCounterData,
            Boolean::class.javaObjectType, false
        )

        var wcDividingCharacter = ConfEntry(
            811, "wcDividingCharacter", ConfSection.warehouseCounterData,
            String::class.javaObjectType, "%$"
        )

        private var wcBackwardCompatibility = ConfEntry(
            812, "wcBackwardCompatibility", ConfSection.warehouseCounterData,
            Boolean::class.javaObjectType, false
        )

        private var wcLogo = ConfEntry(
            813, "Logo", ConfSection.warehouseCounterData,
            String::class.javaObjectType, ""
        )

        private var wcDeskBlackTheme = ConfEntry(
            814, "DeskBlackTheme", ConfSection.warehouseCounterData,
            Boolean::class.javaObjectType, false
        )

        private var wcCompanyData = ConfEntry(
            815, "CompanyData", ConfSection.warehouseCounterData,
            String::class.javaObjectType, ""
        )

        private var wcRequestLot = ConfEntry(
            816, "wcRequestLot", ConfSection.warehouseCounterData,
            Boolean::class.javaObjectType, false
        )

        private var wcRequestPresentation = ConfEntry(
            817, "wcRequestPresentation", ConfSection.warehouseCounterData,
            Boolean::class.javaObjectType, false
        )

        private var wcBarcodeLabelDefault = ConfEntry(
            818, "wcBarcodeLabelDefault", ConfSection.warehouseCounterData,
            String::class.javaObjectType, """
        Primera etiqueta de contenido
        N
        q805
        Datos Lote Caja Recepcion
        B60,20,0,1,4,11,70,B,""#ITEM_ID#""

        A40,130,0,5,1,1,R,""#ITEM_EAN#""
        A40,190,0,3,1,1,R,""#ITEM_PRICE#""

        A40,245,0,2,2,2,N,""#ITEM_DESCRIPTION#""

        A670,275,0,1,1,1,N,""#DATE_PRINTED#""


        Datos contenido
        A220,320,0,5,1,1,N,""DACOSYS""
        A60,390,0,4,1,1,N,""SISTEMAS DE IDENTIFICACION Y RECOLECCION""
        A60,430,0,4,1,1,N,""DE DATOS""
        A60,470,0,4,1,1,N,""http://www.dacosys.com""
        A60,510,0,4,1,1,N,""+54(11)5217-9472/3""
        A60,550,0,4,1,1,N,""info@dacosys.com""

        A60,600,0,5,1,1,N,""ETIQUETA DE""
        A60,650,0,5,1,1,N,""DEMOSTRACION""


        Lineas Etiqueta
        LO20,0,780,5
        LO20,300,780,5
        LO20,520,780,2
        LO20,770,780,5

        LO20,0,5,770
        LO800,0,5,770
        P1
        N
        """
        )

        //</editor-fold>

        //<editor-fold desc="900 - AssetControl Data"
        // APP DATA SYNC DATA
        private var acAutoSend = ConfEntry(
            900, "AutoSend", ConfSection.assetControlData,
            Boolean::class.javaObjectType, false
        )

        private var acLastUpdate = ConfEntry(
            901, "LastUpdate", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        //var acLastUpdateActionLog = ConfEntry(
        //    902, "LastUpdateActionLog", ConfSection.assetControlData,
        //    String.String::class.javaObjectType, Statics.defaultDate)

        var acLastUpdateAsset = ConfEntry(
            903, "LastUpdateAsset", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateAssetMaintenance = ConfEntry(
            956, "LastUpdateAssetManteinance", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateAssetMaintenanceLog = ConfEntry(
            957, "LastUpdateAssetManteinanceLog", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateAssetReview = ConfEntry(
            904, "LastUpdateAssetReview", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateItemCategory = ConfEntry(
            905, "LastUpdateItemCategory", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateMaintenanceType = ConfEntry(
            906, "LastUpdateManteinanceType", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateMaintenanceTypeGroup = ConfEntry(
            907, "LastUpdateManteinanceTypeGroup", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateProviders = ConfEntry(
            908, "LastUpdateProviders", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateRepairman = ConfEntry(
            955, "LastUpdateRepairman", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateRepairshop = ConfEntry(
            909, "LastUpdateRepairshop", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateUser = ConfEntry(
            910, "LastUpdateUser", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateWarehouse = ConfEntry(
            911, "LastUpdateWarehouse", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateWarehouseArea = ConfEntry(
            912, "LastUpdateWarehouseArea", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateWarehouseMovement = ConfEntry(
            913, "LastUpdateWarehouseMovement", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        // AC appdata
        private var acMaxSimulThreads = ConfEntry(
            914, "MaxSimulThreads", ConfSection.assetControlData,
            Int::class.javaObjectType, 3
        )

        private var acPingTimeOut = ConfEntry(
            915, "PingTimeOut", ConfSection.assetControlData,
            Int::class.javaObjectType, 120
        )

        private var acInterval = ConfEntry(
            916, "Interval", ConfSection.assetControlData,
            Int::class.javaObjectType, (60000 * 15)
        )

        private var acDaysToShow = ConfEntry(
            917, "DaysToShow", ConfSection.assetControlData,
            Int::class.javaObjectType, 7
        )

        private var acMaxActionLogs = ConfEntry(
            918, "MaxActionLogs", ConfSection.assetControlData,
            Int::class.javaObjectType, 100
        )

        private var acSoftwareConfigured = ConfEntry(
            919, "SoftwareConfigured", ConfSection.assetControlData,
            Boolean::class.javaObjectType, false
        )

        private var acDownloadDbStartup = ConfEntry(
            920, "DownloadDbStartup", ConfSection.assetControlData,
            Boolean::class.javaObjectType, false
        )

        var acAddLabelNumberOnBarcode = ConfEntry(
            921, "AddLabelNumberOnBarcode", ConfSection.assetControlData,
            Boolean::class.javaObjectType, false
        )

        // REPORT data
        private var acReportAssetAmortization = ConfEntry(
            922, "ReportAssetAmortization", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acReportAssetDischarge = ConfEntry(
            923, "ReportAssetDischarge", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acReportAssetLease = ConfEntry(
            924, "ReportAssetLease", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acReportAssetRental = ConfEntry(
            925, "ReportAssetRental", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acReportAssetReview = ConfEntry(
            926, "ReportAssetReview", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acReportAssetReviewComp = ConfEntry(
            927, "ReportAssetReviewComp", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acReportAssetStatus = ConfEntry(
            928, "ReportAssetStatus", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acReportAssetUnsubscribe = ConfEntry(
            929, "ReportAssetUnsubscribe", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acReportWarehouseMovement = ConfEntry(
            930, "ReportWarehouseMovement", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        // Control data
        private var acControlAsset = ConfEntry(
            931, "ControlAsset", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlUser = ConfEntry(
            932, "ControlUser", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlProvider = ConfEntry(
            933, "ControlProvider", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetLabelPrint = ConfEntry(
            934, "ControlAssetLabelPrint", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetMonitoringMainForm = ConfEntry(
            935, "ControlAssetMonitoringMainForm", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlWarehouseMovementMainForm = ConfEntry(
            936, "ControlWarehouseMovementMainForm", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetReviewComp = ConfEntry(
            937, "ControlAssetReviewComp", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlWarehouseMovementReviewComp = ConfEntry(
            938, "ControlWarehouseMovementReviewComp", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetReviewMainForm = ConfEntry(
            939, "ControlAssetReviewMainForm", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetReviewSelectComp = ConfEntry(
            940, "ControlAssetReviewSelectComp", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlActionLogMainForm = ConfEntry(
            941, "ControlActionLogMainForm", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlWarehouseMovementContent = ConfEntry(
            942, "ControlWarehouseMovementContent", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlWarehouseMovementSelect = ConfEntry(
            943, "ControlWarehouseMovementSelect", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetAmortization = ConfEntry(
            944, "ControlAssetAmortization", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetReviewContent = ConfEntry(
            945, "ControlAssetReviewContent", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetMonitoring = ConfEntry(
            946, "ControlAssetMonitoring", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetSelect = ConfEntry(
            947, "ControlAssetSelect", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetRental = ConfEntry(
            948, "ControlAssetRental", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetLease = ConfEntry(
            949, "ControlAssetLease", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlWarehouseMovementContentSpecial = ConfEntry(
            950, "ControlWarehouseMovementContentSpecial", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlWarehouseMovementContentReport = ConfEntry(
            951, "ControlWarehouseMovementContentReport", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetReviewContentSpecial = ConfEntry(
            952, "ControlAssetReviewContentSpecial", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetMaintenanceProgramed = ConfEntry(
            953, "ControlAssetManteinanceProgramed", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlMaintenanceTypeSelect = ConfEntry(
            954, "ControlManteinanceTypeSelect", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlAssetMovements = ConfEntry(
            955, "ControlAssetMovements", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        private var acControlImageControlDocumentMainForm = ConfEntry(
            956, "ControlImageControlDocumentMainForm", ConfSection.assetControlData,
            String::class.javaObjectType, ""
        )

        var acSyncQtyRegistry = ConfEntry(
            957, "SyncQtyRegistry", ConfSection.assetControlData,
            Int::class.javaObjectType, 75
        )

        private var acMaintenanceDays = ConfEntry(
            958, "ManteinanceDays", ConfSection.assetControlData,
            Int::class.javaObjectType, 7
        )

        var acLastUpdateCostCentre = ConfEntry(
            959, "LastUpdateCostCentre", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateAssetMaintenanceProgramed = ConfEntry(
            960, "LastUpdateAssetManteinanceProgramed", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acLastUpdateAssetReviewContent = ConfEntry(
            961, "LastUpdateAssetReviewContent", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateAttribute = ConfEntry(
            962, "LastUpdateAttribute", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateAttributeCategory = ConfEntry(
            963, "LastUpdateAttributeCategory", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateRoute = ConfEntry(
            964, "LastUpdateRoute", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateDataCollectionRule = ConfEntry(
            965, "LastUpdateDataCollectionRule", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acControlRoute =
            ConfEntry(
                966, "ControlRoute", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        private var acControlDataCollectionRule =
            ConfEntry(
                967, "ControlDataCollectionRule", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        var acLastUpdateDataCollection = ConfEntry(
            968, "LastUpdateDataCollection", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acControlDataCollection =
            ConfEntry(
                969, "ControlDataCollection", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        private var acControlDataCollectionContent =
            ConfEntry(
                970, "ControlDataCollectionContent", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        private var acControlDataCollectionMainForm =
            ConfEntry(
                971, "ControlDataCollectionMainForm", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        var acLastUpdateRouteProcess = ConfEntry(
            972, "LastUpdateRouteProcess", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acControlRouteProcessMainForm =
            ConfEntry(
                973, "ControlRouteProcessMainForm", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        private var acControlRouteProcessContent =
            ConfEntry(
                974, "ControlRouteProcessContent", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        private var acLastUpdateDataCollectionReport = ConfEntry(
            975, "LastUpdateDataCollectionReport", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acControlDataCollectionReport =
            ConfEntry(
                976, "ControlDataCollectionReport", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        private var acControlDataCollectionReportMainForm =
            ConfEntry(
                977, "ControlDataCollectionReportMainForm", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        private var acControlErrorLogMainForm =
            ConfEntry(
                978, "ErrorLogMainFor", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        private var acLastUpdateWarehouseMovementContent = ConfEntry(
            979, "LastUpdateWarehouseMovementContent", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acLastUpdateRouteProcessContent = ConfEntry(
            980, "LastUpdateRouteProcessContent", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acLastUpdateDataCollectionContent = ConfEntry(
            981, "LastUpdateDataCollectionContent", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        var acLastUpdateBarcodeLabelCustom = ConfEntry(
            982, "LastUpdateBarcodeLabelCustom", ConfSection.assetControlData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acControlBarcodeLabelCustom =
            ConfEntry(
                983, "ControlBarcodeLabelCustom", ConfSection.assetControlData,
                String::class.javaObjectType, ""
            )

        var acLastUpdateUserWarehouseArea =
            ConfEntry(
                984, "LastUpdateUserWarehouseArea", ConfSection.assetControlData,
                String::class.javaObjectType, Statics.DEFAULT_DATE
            )

        //</editor-fold>

        //<editor-fold desc="1000 - WebService 2 Data"
        private var ws2User = ConfEntry(
            1000, "User", ConfSection.webservice2Data,
            String::class.javaObjectType, ""
        )

        private var ws2Pass = ConfEntry(
            1001, "Pass", ConfSection.webservice2Data,
            String::class.javaObjectType, ""
        )

        private var ws2DB = ConfEntry(
            1002, "DB", ConfSection.webservice2Data,
            String::class.javaObjectType, ""
        )

        private var ws2Url = ConfEntry(
            1003, "Server", ConfSection.webservice2Data,
            String::class.javaObjectType, ""
        )

        private var ws2Port = ConfEntry(
            1004, "Port", ConfSection.webservice2Data,
            Int::class.javaObjectType, 0
        )

        private var ws2ConnectionLifetime = ConfEntry(
            1005, "ConnectionLifetime", ConfSection.webservice2Data,
            Int::class.javaObjectType, 100
        )

        private var ws2ConnectTimeout = ConfEntry(
            1006, "ConnectTimeout", ConfSection.webservice2Data,
            Int::class.javaObjectType, 10
        )

        private var ws2DefaultCommandTimeout = ConfEntry(
            1007, "DefaultCommandTimeout", ConfSection.webservice2Data,
            Int::class.javaObjectType, 10
        )

        private var ws2MaxReceivedMessageSize = ConfEntry(
            1008, "MaxReceivedMessageSize", ConfSection.webservice2Data,
            Int::class.javaObjectType, 64
        )

        private var ws2MaxBufferSize = ConfEntry(
            1009, "MaxBufferSize", ConfSection.webservice2Data,
            Int::class.javaObjectType, 64
        )

        private var ws2SkipPing = ConfEntry(
            1010, "SkipPing", ConfSection.webservice2Data,
            Boolean::class.javaObjectType, false
        )

        //</editor-fold>

        //<editor-fold desc="1100 - WebService 2 Proxy Data"
        private var ws2ProxyUse = ConfEntry(
            1100, "ProxyUse", ConfSection.proxyWebService2Data,
            Boolean::class.javaObjectType, false
        )

        private var ws2ProxyUseDefaultProxy = ConfEntry(
            1101, "UseDefaultProxy", ConfSection.proxyWebService2Data,
            Boolean::class.javaObjectType, true
        )

        private var ws2ProxyUrl = ConfEntry(
            1102, "ProxyUrl", ConfSection.proxyWebService2Data,
            String::class.javaObjectType, ""
        )

        private var ws2ProxyPort = ConfEntry(
            1103, "ProxyPort", ConfSection.proxyWebService2Data,
            Int::class.javaObjectType, 80
        )

        private var ws2ProxyUseCredentials = ConfEntry(
            1104, "ProxyUseCredentials", ConfSection.proxyWebService2Data,
            Boolean::class.javaObjectType, false
        )

        private var ws2ProxyUseDefaultCredentials = ConfEntry(
            1105, "ProxyUseDefaultCredentials", ConfSection.proxyWebService2Data,
            Boolean::class.javaObjectType, true
        )

        private var ws2ProxyUser = ConfEntry(
            1106, "ProxyUser", ConfSection.proxyWebService2Data,
            String::class.javaObjectType, ""
        )

        private var ws2ProxyPass = ConfEntry(
            1107, "ProxyPass", ConfSection.proxyWebService2Data,
            String::class.javaObjectType, ""
        )

        private var ws2ProxyDomain = ConfEntry(
            1108, "ProxyDomain", ConfSection.proxyWebService2Data,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="1200 - Printer Data"

        var prnPrinterPower = ConfEntry(
            1200, "PrinterPower", ConfSection.printerData,
            Int::class.javaObjectType, 10
        )

        var prnPrinterSpeed = ConfEntry(
            1201, "PrinterSpeed", ConfSection.printerData,
            Int::class.javaObjectType, 1
        )

        //var prnPrinterRest = ConfEntry(
        //    1202, "PrinterRest", ConfSection.printerData,
        //    Int.Int::class.javaObjectType, 0)

        var prnPrinterName = ConfEntry(
            1203, "PrinterName", ConfSection.printerData,
            String::class.javaObjectType, ""
        )

        private var prnPrinterBrotherName = ConfEntry(
            1204, "PrinterBrotherName", ConfSection.printerData,
            String::class.javaObjectType, ""
        )

        private var prnZebraOrBrother = ConfEntry(
            1205, "ZebraOrBrother", ConfSection.printerData,
            Boolean::class.javaObjectType, false
        )

        private var prnBrotherTapeWidth = ConfEntry(
            1206, "BrotherTapeWidth", ConfSection.printerData,
            Int::class.javaObjectType, 12
        )

        private var prnBTCommPort = ConfEntry(
            1207, "Bluetooth Comm Port", ConfSection.printerData,
            String::class.javaObjectType, "COM0"
        )

        private var prnBTBaud = ConfEntry(
            1208, "Bluetooth Baud", ConfSection.printerData,
            Int::class.javaObjectType, 115200
        )

        private var prnBTDefault = ConfEntry(
            1209, "Default Printer Bluetooth ", ConfSection.printerData,
            Boolean::class.javaObjectType, true
        )

        private var prnNetIP = ConfEntry(
            1210, "Network Printer IP", ConfSection.printerData,
            String::class.javaObjectType, "10.10.10.10"
        )

        private var prnNetPort = ConfEntry(
            1211, "Network Printer Port", ConfSection.printerData,
            Int::class.javaObjectType, 9100
        )

        private var prnNetDefault = ConfEntry(
            1212, "Default Printer Network ", ConfSection.printerData,
            Boolean::class.javaObjectType, false
        )

        var prnColOffset = ConfEntry(
            1213, "Column Offset", ConfSection.printerData,
            Int::class.javaObjectType, 0
        )

        var prnRowOffset = ConfEntry(
            1214, "Row Offset", ConfSection.printerData,
            Int::class.javaObjectType, 0
        )

        private var prnLineEndChar = ConfEntry(
            1215, "Line End Char", ConfSection.printerData,
            Int::class.javaObjectType, 10
        )

        //</editor-fold>

        //<editor-fold desc="1300 - AssetControl Maintenance Data"

        private var acmControlAsset = ConfEntry(
            1300, "ControlAsset", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlRepairman = ConfEntry(
            1301, "ControlRepairman", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlAssetWithMaintenanceProgramedMainForm = ConfEntry(
            1302, "ControlAssetWithManteinanceProgramed", ConfSection.assetControlMainData,
            Int::class.javaObjectType,
            ""
        )

        private var acmControlAssetOnRepairshopMainForm = ConfEntry(
            1303, "ControlAssetOnRepairshop", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlAssetMaintenanceMainForm = ConfEntry(
            1304, "ControlAssetManteinance", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlActionLogMainForm = ConfEntry(
            1305, "ControlActionLog", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlAssetSelect = ConfEntry(
            1306, "ControlAssetSelect", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlAssetInGeneral = ConfEntry(
            1307, "ControlAssetInGeneral", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlAssetMaintenanceProgramed = ConfEntry(
            1308, "ControlAssetManteinanceProgramed", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlMaintenanceTypeSelect = ConfEntry(
            1309, "ControlManteinanceTypeSelect", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlAssetReceptionContentSpecial = ConfEntry(
            1310, "ControlAssetReceptionContentSpecial", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlAssetRemissionContentSpecial = ConfEntry(
            1311, "ControlAssetRemissionContentSpecial", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlWarehouseMovementContent = ConfEntry(
            1312, "ControlWarehouseMovementContent", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlWarehouseMovementSelect = ConfEntry(
            1313, "ControlWarehouseMovementSelect", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmReportAssetMaintenance = ConfEntry(
            1314, "ReportAssetManteinance", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmReportWarehouseMovement = ConfEntry(
            1315, "ReportWarehouseMovement", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmReportAssetMaintenanceProgramed = ConfEntry(
            1316, "ReportAssetManteinanceProgramed", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmControlAssetMaintenanceLog = ConfEntry(
            1317, "ControlAssetManteinanceLog", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        private var acmImageControlDocument = ConfEntry(
            1318, "ImageControlDocument", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        // ACM AppData
        private var acmInterval = ConfEntry(
            1319, "Interval", ConfSection.assetControlMainData,
            Int::class.javaObjectType, (60000 * 15)
        )

        private var acmPingTimeOut = ConfEntry(
            1320, "PingTimeOut", ConfSection.assetControlMainData,
            Int::class.javaObjectType, 120
        )

        private var acmDownloadDbStartup = ConfEntry(
            1321, "DownloadDbStartup", ConfSection.assetControlMainData,
            Boolean::class.javaObjectType, false
        )

        private var acmLastUpdateAsset = ConfEntry(
            1322, "LastUpdateAsset", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateAssetMaintenance = ConfEntry(
            1323, "LastUpdateAssetManteinance", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateAssetMaintenanceLog = ConfEntry(
            1324, "LastUpdateAssetManteinanceLog", ConfSection.assetControlMainData,
            Int::class.javaObjectType,
            Statics.DEFAULT_DATE
        )

        private var acmLastUpdateItemCategory = ConfEntry(
            1325, "LastUpdateItemCategory", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateMaintenanceType = ConfEntry(
            1326, "LastUpdateManteinanceType", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateMaintenanceTypeGroup = ConfEntry(
            1327, "LastUpdateManteinanceTypeGroup", ConfSection.assetControlMainData,
            Int::class.javaObjectType,
            Statics.DEFAULT_DATE
        )

        private var acmLastUpdateProviders = ConfEntry(
            1328, "LastUpdateProviders", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateRepairman = ConfEntry(
            1329, "LastUpdateRepairman", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateRepairshop = ConfEntry(
            1330, "LastUpdateRepairshop", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateUser = ConfEntry(
            1331, "LastUpdateUser", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateWarehouse = ConfEntry(
            1332, "LastUpdateWarehouse", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateWarehouseArea = ConfEntry(
            1333, "LastUpdateWarehouseArea", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmLastUpdateWarehouseMovement = ConfEntry(
            1334, "LastUpdateWarehouseMovement", ConfSection.assetControlMainData,
            Int::class.javaObjectType,
            Statics.DEFAULT_DATE
        )

        private var acmLastUpdateWarehouseMovementContent = ConfEntry(
            1335, "LastUpdateWarehouseMovementContent", ConfSection.assetControlMainData,
            Int::class.javaObjectType,
            Statics.DEFAULT_DATE
        )

        private var acmManteinanceDays = ConfEntry(
            1336, "ManteinanceDays", ConfSection.assetControlMainData,
            Int::class.javaObjectType, 7
        )

        private var acmDaysToShow = ConfEntry(
            1337, "DaysToShow", ConfSection.assetControlMainData,
            Int::class.javaObjectType, 7
        )

        private var acmMaxActionLogs = ConfEntry(
            1338, "MaxActionLogs", ConfSection.assetControlMainData,
            Int::class.javaObjectType, 100
        )

        private var acmSoftwareConfigured = ConfEntry(
            1339, "SoftwareConfigured", ConfSection.assetControlMainData,
            Boolean::class.javaObjectType, false
        )

        private var acmAddLabelNumberOnBarcode = ConfEntry(
            1340, "AddLabelNumberOnBarcode", ConfSection.assetControlMainData,
            Boolean::class.javaObjectType, false
        )

        private var acmSyncQtyRegistry = ConfEntry(
            1341, "SyncQtyRegistry", ConfSection.assetControlMainData,
            Int::class.javaObjectType, 20
        )

        private var acmMaxSimulThreads = ConfEntry(
            1342, "MaxSimulThreads", ConfSection.assetControlMainData,
            Int::class.javaObjectType, 3
        )

        private var acmLastUpdate = ConfEntry(
            1343, "LastUpdate", ConfSection.assetControlMainData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var acmControlErrorLogMainForm = ConfEntry(
            1344, "ControlErrorLogMainForm", ConfSection.assetControlMainData,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="1400 - Presale Data"

        // APP DATA SYNC DATA
        private var psAutoSend = ConfEntry(
            1400, "AutoSend", ConfSection.presaleData,
            Boolean::class.javaObjectType, false
        )

        private var psLastUpdate = ConfEntry(
            1401, "LastUpdate", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psMinCharCountSearch = ConfEntry(
            1403, "MinCharCountSearch", ConfSection.presaleData,
            Int::class.javaObjectType, 3
        )

        private var psInterval = ConfEntry(
            1404, "SyncInterval", ConfSection.presaleData,
            Int::class.javaObjectType, 60000 * 15
        )

        private var psDaysToShow = ConfEntry(
            1405, "DaysToShow", ConfSection.presaleData,
            Int::class.javaObjectType, 7
        )

        private var psSoftwareConfigured = ConfEntry(
            1406, "SoftwareConfigured", ConfSection.presaleData,
            Boolean::class.javaObjectType, false
        )

        private var psLastUpdateClient = ConfEntry(
            1407, "LastUpdateClients", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psLastUpdateItem = ConfEntry(
            1408, "LastUpdateItems", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psLastUpdateSalesman = ConfEntry(
            1409, "LastUpdateSalesmans", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psLastUpdateRoute = ConfEntry(
            1410, "LastUpdateRoutes", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psLastUpdateCountry = ConfEntry(
            1411, "LastUpdateCountries", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psLastUpdateOrder = ConfEntry(
            1412, "LastUpdateOrders", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psLastUpdateReturn = ConfEntry(
            1413, "LastUpdateReturns", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psLastUpdatePayment = ConfEntry(
            1414, "LastUpdatePayments", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psLastUpdateItemCategory = ConfEntry(
            1415, "LastUpdateItemsCategory", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psLastUpdateSalesmanRouteLog = ConfEntry(
            1416, "LastUpdateSalesmanRouteLog", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var psSyncThreads = ConfEntry(
            1417, "SyncThreads", ConfSection.presaleData,
            Int::class.javaObjectType, 3
        )

        private var psSyncOnEveryRequest = ConfEntry(
            1418, "SyncOnAllRequests", ConfSection.presaleData,
            Boolean::class.javaObjectType, true
        )

        private var psClient = ConfEntry(
            1419, "ControlClient", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psClientSelect = ConfEntry(
            1420, "ControlClientSelect", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psClientMultiSelect = ConfEntry(
            1421, "ControlClientMultiSelect", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psItem = ConfEntry(
            1422, "ControlItem", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psItemSelect = ConfEntry(
            1423, "ControlItemSelect", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psOrderMain = ConfEntry(
            1424, "ControlOrderMain", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psReturnMain = ConfEntry(
            1425, "ControlReturnMain", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psPaymentMain = ConfEntry(
            1426, "ControlPaymentMain", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psSalesmanRouteLogResumeMain = ConfEntry(
            1427, "ControlSalesmanRouteLogMain", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psImageControlDocument = ConfEntry(
            1428, "ControlImageControlDocument", ConfSection.presaleData,
            String::class.javaObjectType, ""
        )

        private var psSyncQtyRegistry = ConfEntry(
            1429, "SyncQtyRegistry", ConfSection.presaleData,
            Int::class.javaObjectType, 20
        )

        private var psMaxSimulThreads = ConfEntry(
            1430, "MaxSimulThreads", ConfSection.presaleData,
            Int::class.javaObjectType, 3
        )

        private var psPingTimeOut = ConfEntry(
            1431, "PingTimeOut", ConfSection.presaleData,
            Int::class.javaObjectType, 120
        )

        private var psLastUpdatePromotion = ConfEntry(
            1432, "LastUpdatePromotion", ConfSection.presaleData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        //</editor-fold>

        //<editor-fold desc="1500 - Medidor Movil Data"

        private var mmLastUpdate = ConfEntry(
            1500, "LastUpdate", ConfSection.medidorMovilData,
            String::class.javaObjectType, Statics.DEFAULT_DATE
        )

        private var mmRadGridClient = ConfEntry(
            1501, "RadConfClient", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridMeter = ConfEntry(
            1502, "RadConfMeter", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridUser = ConfEntry(
            1503, "RadConfUser", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridClientSelect = ConfEntry(
            1504, "RadConfClientSelect", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridMeterSelect = ConfEntry(
            1505, "RadConfMeterSelect", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridUserSelect = ConfEntry(
            1506, "RadConfUserSelect", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridStreet = ConfEntry(
            1507, "RadConfStreet", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridRoute = ConfEntry(
            1508, "RadConfRoute", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmDaysToShow = ConfEntry(
            1509, "DaysToShow", ConfSection.medidorMovilData,
            String::class.javaObjectType, "15"
        )

        private var mmSoftwareConfigured = ConfEntry(
            1510, "SoftwareConfigured", ConfSection.medidorMovilData,
            Boolean::class.javaObjectType, false
        )

        private var mmDeleteOrderAfterDownload = ConfEntry(
            1511, "DeleteOrderAfterDownload", ConfSection.medidorMovilData,
            Boolean::class.javaObjectType, false
        )

        private var mmSeparatorCharacter = ConfEntry(
            1512, "SeparatorCharacter", ConfSection.medidorMovilData,
            String::class.javaObjectType, "%$"
        )

        private var mmRadGridReadingMain = ConfEntry(
            1513, "RadGridReadingMain", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridMeterMain = ConfEntry(
            1514, "RadGridMeterMain", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmLogo = ConfEntry(
            1515, "Logo", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmDeskBlackTheme = ConfEntry(
            1516, "DeskBlackTheme", ConfSection.medidorMovilData,
            Boolean::class.javaObjectType, false
        )

        private var mmCompanyData = ConfEntry(
            1517, "CompanyData", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridFtpFileSelect = ConfEntry(
            1518, "RadGridFtpFileSelect", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridCity = ConfEntry(
            1519, "RadConfCity", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        private var mmRadGridState = ConfEntry(
            1520, "RadConfState", ConfSection.medidorMovilData,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="1600 - FTP Data"

        private var ftpUser = ConfEntry(
            1600, "FTPUser", ConfSection.ftpData,
            String::class.javaObjectType, ""
        )

        private var ftpPass = ConfEntry(
            1601, "FTPPass", ConfSection.ftpData,
            String::class.javaObjectType, ""
        )

        private var ftpFolder = ConfEntry(
            1602, "FTPFolder", ConfSection.ftpData,
            String::class.javaObjectType, "/"
        )

        private var ftpServer = ConfEntry(
            1603, "Server", ConfSection.ftpData,
            String::class.javaObjectType, ""
        )

        private var ftpPort = ConfEntry(
            1604, "Port", ConfSection.ftpData,
            Int::class.javaObjectType, 21
        )

        //</editor-fold>

        //<editor-fold desc="1700 - Mysql Data 2"

        private var my2User = ConfEntry(
            1700, "User", ConfSection.mySqlConnection2Data,
            String::class.javaObjectType, ""
        )

        private var my2Pass = ConfEntry(
            1701, "Pass", ConfSection.mySqlConnection2Data,
            String::class.javaObjectType, ""
        )

        private var my2DB = ConfEntry(
            1702, "DB", ConfSection.mySqlConnection2Data,
            String::class.javaObjectType, ""
        )

        private var my2Server = ConfEntry(
            1703, "Server", ConfSection.mySqlConnection2Data,
            String::class.javaObjectType, ""
        )

        private var my2Port = ConfEntry(
            1704, "Port", ConfSection.mySqlConnection2Data,
            Int::class.javaObjectType, 0
        )

        private var my2ConnectionLifetime = ConfEntry(
            1705, "ConnectionLifetime", ConfSection.mySqlConnection2Data,
            Int::class.javaObjectType, 100
        )

        private var my2ConnectTimeout = ConfEntry(
            1706, "ConnectTimeout", ConfSection.mySqlConnection2Data,
            Int::class.javaObjectType, 10
        )

        private var my2DefaultCommandTimeout = ConfEntry(
            1707, "DefaultCommandTimeout", ConfSection.mySqlConnection2Data,
            Int::class.javaObjectType, 10
        )

        private var my2DBConf = ConfEntry(
            1708, "DBConf", ConfSection.mySqlConnection2Data,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="1800 - WebService ImageControl Proxy Data"

        private var wsIcProxyUse = ConfEntry(
            1800, "ProxyUse", ConfSection.proxyWebServiceData,
            Boolean::class.javaObjectType, false
        )

        private var wsIcProxyUseDefaultProxy = ConfEntry(
            1801, "UseDefaultProxy", ConfSection.proxyWebServiceData,
            Boolean::class.javaObjectType, true
        )

        private var wsIcProxyUrl = ConfEntry(
            1802, "ProxyUrl", ConfSection.proxyWebServiceData,
            String::class.javaObjectType, ""
        )

        private var wsIcProxyPort = ConfEntry(
            1803, "ProxyPort", ConfSection.proxyWebServiceData,
            Int::class.javaObjectType, 80
        )

        private var wsIcProxyUseCredentials = ConfEntry(
            1804, "ProxyUseCredentials", ConfSection.proxyWebServiceData,
            Boolean::class.javaObjectType, false
        )

        private var wsIcProxyUseDefaultCredentials = ConfEntry(
            1805, "ProxyUseDefaultCredentials", ConfSection.proxyWebServiceData,
            Boolean::class.javaObjectType, true
        )

        private var wsIcProxyUser = ConfEntry(
            1806, "ProxyUser", ConfSection.proxyWebServiceData,
            String::class.javaObjectType, ""
        )

        private var wsIcProxyPass = ConfEntry(
            1807, "ProxyPass", ConfSection.proxyWebServiceData,
            String::class.javaObjectType, ""
        )

        private var wsIcProxyDomain = ConfEntry(
            1808, "ProxyDomain", ConfSection.proxyWebServiceData,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="1900 - WebService ImageControl Data"

        private var wsIcUser = ConfEntry(
            1900, "User", ConfSection.imageControlData,
            String::class.javaObjectType, ""
        )

        private var wsIcPass = ConfEntry(
            1901, "Pass", ConfSection.imageControlData,
            String::class.javaObjectType, ""
        )

        private var wsIcDB = ConfEntry(
            1902, "DB", ConfSection.imageControlData,
            String::class.javaObjectType, ""
        )

        private var wsIcUrl = ConfEntry(
            1903, "Server", ConfSection.imageControlData,
            String::class.javaObjectType, ""
        )

        private var wsIcPort = ConfEntry(
            1904, "Port", ConfSection.imageControlData,
            Int::class.javaObjectType, 0
        )

        private var wsIcConnectionLifetime = ConfEntry(
            1905, "ConnectionLifetime", ConfSection.imageControlData,
            Int::class.javaObjectType, 100
        )

        private var wsIcConnectTimeout = ConfEntry(
            1906, "ConnectTimeout", ConfSection.imageControlData,
            Int::class.javaObjectType, 10
        )

        private var wsIcDefaultCommandTimeout = ConfEntry(
            1907, "DefaultCommandTimeout", ConfSection.imageControlData,
            Int::class.javaObjectType, 10
        )

        private var wsIcMaxReceivedMessageSize = ConfEntry(
            1908, "MaxReceivedMessageSize", ConfSection.imageControlData,
            Int::class.javaObjectType, 64
        )

        private var wsIcMaxBufferSize = ConfEntry(
            1909, "MaxBufferSize", ConfSection.imageControlData,
            Int::class.javaObjectType, 64
        )

        private var wsIcSkipPing = ConfEntry(
            1910, "SkipPing", ConfSection.imageControlData,
            Boolean::class.javaObjectType, false
        )

        private var icUser = ConfEntry(
            1911, "ImageControlUser", ConfSection.imageControlData,
            String::class.javaObjectType, ""
        )

        private var icPass = ConfEntry(
            1912, "ImageControlPass", ConfSection.imageControlData,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="2000 - Language"

        var langUILanguage = ConfEntry(
            2000, "UILanguage", ConfSection.languageData,
            String::class.javaObjectType, "es-AR"
        ) // default es_AR

        var langUILanguageMode = ConfEntry(
            2001, "UILanguageMode", ConfSection.languageData,
            Int::class.javaObjectType, 2
        ) // default show

        //</editor-fold>

        //<editor-fold desc="2100 LDAP"

        private var ldapDomain = ConfEntry(
            2100, "LDAPDomain", ConfSection.ldapData,
            String::class.javaObjectType, ""
        )

        private var ldapUser = ConfEntry(
            2101, "LDAPUser", ConfSection.ldapData,
            String::class.javaObjectType, ""
        )

        private var ldapPass = ConfEntry(
            2102, "LDAPPass", ConfSection.ldapData,
            String::class.javaObjectType, ""
        )

        private var ldapUserCredential = ConfEntry(
            2103, "LDAPUseUserCredential", ConfSection.ldapData,
            Boolean::class.javaObjectType, true
        )

        //</editor-fold>

        //<editor-fold desc="2200 StockControlCE DataGrid"

        private var SCCEDgBoxReceptionContent = ConfEntry(
            2200, "SCCEDgBoxReceptionContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgBoxReceptionContentResume = ConfEntry(
            2201, "SCCEDgBoxReceptionContentResume", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgOrderContent = ConfEntry(
            2202, "SCCEDgOrderContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgOrderSelector = ConfEntry(
            2203, "SCCEDgOrderSelector", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgOrderPackagingWacOrderContent = ConfEntry(
            2204, "SCCEDgOrderPackagingWacOrderContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgOrderPackagingWacBoxContent = ConfEntry(
            2205, "SCCEDgOrderPackagingWacBoxContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgReserveSelector = ConfEntry(
            2206, "SCCEDgReserveSelector", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgStockOrder = ConfEntry(
            2207, "SCCEDgStockOrder", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgSuggestedLocationSelector = ConfEntry(
            2208, "SCCEDgSuggestedLocationSelector", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWarehouseMovementDeliveryContent = ConfEntry(
            2209, "SCCEDgWarehouseMovementDeliveryContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWarehouseMovementInternalContent = ConfEntry(
            2210, "SCCEDgWarehouseMovementInternalContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWarehouseMovementReceptionContent = ConfEntry(
            2211, "SCCEDgWarehouseMovementReceptionContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWarehouseMovementRequestContent = ConfEntry(
            2212, "SCCEDgWarehouseMovementRequestContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWarehouseMovementRequest = ConfEntry(
            2213, "SCCEDgWarehouseMovementRequest", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWmrContentModify = ConfEntry(
            2214, "SCCEDgWmrContentModify", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgItemSelect = ConfEntry(
            2216, "SCCEDgItemSelect", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgLotSelect = ConfEntry(
            2217, "SCCEDgLotSelect", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgProviderSelect = ConfEntry(
            2218, "SCCEDgProviderSelect", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgStockOrderContent = ConfEntry(
            2219, "SCCEDgStockOrderContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWacSearch = ConfEntry(
            2220, "SCCEDgWacSearch", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWacSelect = ConfEntry(
            2221, "SCCEDgWacSelect", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWarehouseMovementResume = ConfEntry(
            2222, "SCCEDgWWarehouseMovementResume", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgOrderPackagingSelector = ConfEntry(
            2223, "SCCEDgOrderPackagingSelector", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgSetup = ConfEntry(
            2224, "SCCEDgSetup", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var SCCEDgWarehouseMovementRequestContentLog = ConfEntry(
            2225, "SCCEDgWarehouseMovementRequestContentLog", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        //<editor-fold desc="2300 AssetControlCE DataGrid"

        private var ACCEDgAsset = ConfEntry(
            2300, "ACCEDgAsset", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgRoute = ConfEntry(
            2301, "ACCEDgRoute", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgRouteProcessContent = ConfEntry(
            2302, "ACCEDgRouteProcessContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgDataCollectionRule = ConfEntry(
            2303, "ACCEDgDataCollectionRule", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgAssetReviewContent = ConfEntry(
            2304, "ACCEDgAssetReviewContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgWarehouseMovementContent = ConfEntry(
            2305, "ACCEDgWarehouseMovementContent", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgSelectWarehouseArea = ConfEntry(
            2306, "ACCEDgSelectWarehouseArea", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgSelectOriginDestWarehouse = ConfEntry(
            2307, "ACCEDgSelectOriginDestWarehouse", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgPrintWarehouse = ConfEntry(
            2308, "ACCEDgPrintWarehouse", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgAssetMaintenance = ConfEntry(
            2309, "ACCEDgAssetManteinance", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgSetup = ConfEntry(
            2310, "ACCEDgSetup", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        private var ACCEDgAssetReview = ConfEntry(
            2311, "ACCEDgAssetReview", ConfSection.dataGridCEData,
            String::class.javaObjectType, ""
        )

        //</editor-fold>

        private fun getAllGeneral(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                appShowExitButton,
                appShowConfigurationButton,
                appLogFile,
                appUseFreeSuggestedLocation,
                appHandSignature,
                appShowDownloadDBButton,
                appConfigurationPassword,
                appShowFullLocation,
                appMissingAssetsDestWarehouseArea,

                //COLLECTOR DATA
                collCollectorType,
                collRfidInstalled,
                collRfidPort,
                collRFIDInventoryMode,
                collRFIDFieldStrengthDb,

                //WEBSERVICE
                wsUser,
                wsPass,
                wsDB,
                wsUrl,
                wsPort,
                wsConnectionLifetime,
                wsConnectTimeout,
                wsDefaultCommandTimeout,
                wsMaxReceivedMessageSize,
                wsMaxBufferSize,
                wsSkipPing,
                wsDateTimeFormat,

                //MYSQL
                myUser,
                myPass,
                myDB,
                myServer,
                myPort,
                myConnectionLifetime,
                myConnectTimeout,
                myDefaultCommandTimeout,
                myDBConf,

                //WEBSERVICE PROXY DATA
                wsProxyDomain,
                wsProxyPass,
                wsProxyPort,
                wsProxyUrl,
                wsProxyUse,
                wsProxyUseCredentials,
                wsProxyUseDefaultCredentials,
                wsProxyUseDefaultProxy,
                wsProxyUser,

                //MYSQL PROXY DATA
                //myProxyDomain,
                //myProxyPass,
                //myProxyPort,
                //myProxyUrl,
                //myProxyUse,
                //myProxyUseCredentials,
                //myProxyUseDefaultCredentials,
                //myProxyUseDefaultProxy,
                //myProxyUser,

                //License Data
                liCodeA,
                liCodeB,
                liCodeAN,

                // WEB SERVICE 2
                ws2User,
                ws2Pass,
                ws2DB,
                ws2Url,
                ws2Port,
                ws2ConnectionLifetime,
                ws2ConnectTimeout,
                ws2DefaultCommandTimeout,
                ws2MaxReceivedMessageSize,
                ws2MaxBufferSize,
                ws2SkipPing,

                // WEB SERVICe 2 PROXY DATA
                ws2ProxyDomain,
                ws2ProxyPass,
                ws2ProxyPort,
                ws2ProxyUrl,
                ws2ProxyUse,
                ws2ProxyUseCredentials,
                ws2ProxyUseDefaultCredentials,
                ws2ProxyUseDefaultProxy,
                ws2ProxyUser,

                // PRINTER DATA
                prnPrinterPower,
                prnPrinterSpeed,
                //prnPrinterRest,
                prnPrinterName,
                prnPrinterBrotherName,
                prnZebraOrBrother,
                prnBrotherTapeWidth,
                prnBTCommPort,
                prnBTBaud,
                prnBTDefault,
                prnNetIP,
                prnNetPort,
                prnNetDefault,
                prnColOffset,
                prnRowOffset,
                prnLineEndChar,

                // FTP DATA
                ftpFolder,
                ftpPass,
                ftpPort,
                ftpServer,
                ftpUser,

                //MYSQL 2
                my2User,
                my2Pass,
                my2DB,
                my2Server,
                my2Port,
                my2ConnectionLifetime,
                my2ConnectTimeout,
                my2DefaultCommandTimeout,
                my2DBConf,

                //LDAP
                ldapDomain,
                ldapUser,
                ldapPass,
                ldapUserCredential,

                //DATAGRID StockControlCE
                SCCEDgBoxReceptionContent,
                SCCEDgBoxReceptionContentResume,
                SCCEDgOrderContent,
                SCCEDgOrderSelector,
                SCCEDgOrderPackagingSelector,
                SCCEDgOrderPackagingWacOrderContent,
                SCCEDgOrderPackagingWacBoxContent,
                SCCEDgReserveSelector,
                SCCEDgStockOrder,
                SCCEDgSuggestedLocationSelector,
                SCCEDgWarehouseMovementDeliveryContent,
                SCCEDgWarehouseMovementInternalContent,
                SCCEDgWarehouseMovementReceptionContent,
                SCCEDgWarehouseMovementRequestContent,
                SCCEDgWarehouseMovementRequestContentLog,
                SCCEDgWarehouseMovementRequest,
                SCCEDgWmrContentModify,
                SCCEDgItemSelect,
                SCCEDgLotSelect,
                SCCEDgProviderSelect,
                SCCEDgStockOrderContent,
                SCCEDgWacSearch,
                SCCEDgWacSelect,
                SCCEDgWarehouseMovementResume,
                SCCEDgSetup,

                //DATAGRID AssetControlCE
                ACCEDgAsset,
                ACCEDgRoute,
                ACCEDgRouteProcessContent,
                ACCEDgDataCollectionRule,
                ACCEDgAssetReviewContent,
                ACCEDgWarehouseMovementContent,
                ACCEDgSelectWarehouseArea,
                ACCEDgSelectOriginDestWarehouse,
                ACCEDgPrintWarehouse,
                ACCEDgAssetMaintenance,
                ACCEDgSetup,
                ACCEDgAssetReview
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getAllAssetControlDataGridCe(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                ACCEDgAsset,
                ACCEDgRoute,
                ACCEDgRouteProcessContent,
                ACCEDgDataCollectionRule,
                ACCEDgAssetReviewContent,
                ACCEDgWarehouseMovementContent,
                ACCEDgSelectWarehouseArea,
                ACCEDgSelectOriginDestWarehouse,
                ACCEDgPrintWarehouse,
                ACCEDgAssetMaintenance,
                ACCEDgSetup,
                ACCEDgAssetReview
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getAllStockControlDataGridCe(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                SCCEDgBoxReceptionContent,
                SCCEDgBoxReceptionContentResume,
                SCCEDgOrderContent,
                SCCEDgOrderSelector,
                SCCEDgOrderPackagingSelector,
                SCCEDgOrderPackagingWacOrderContent,
                SCCEDgOrderPackagingWacBoxContent,
                SCCEDgReserveSelector,
                SCCEDgStockOrder,
                SCCEDgSuggestedLocationSelector,
                SCCEDgWarehouseMovementDeliveryContent,
                SCCEDgWarehouseMovementInternalContent,
                SCCEDgWarehouseMovementReceptionContent,
                SCCEDgWarehouseMovementRequestContent,
                SCCEDgWarehouseMovementRequestContentLog,
                SCCEDgWarehouseMovementRequest,
                SCCEDgWmrContentModify,
                SCCEDgItemSelect,
                SCCEDgLotSelect,
                SCCEDgProviderSelect,
                SCCEDgStockOrderContent,
                SCCEDgWacSearch,
                SCCEDgWacSelect,
                SCCEDgWarehouseMovementResume,
                SCCEDgSetup
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getAllLdap(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                //LDAP DATA
                ldapDomain,
                ldapUser,
                ldapPass,
                ldapUserCredential
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getAllWarehouseCounter(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                //WAREHOUSE COUNTER DATA
                wcLastUpdate,
                wcRadConfClient,
                wcRadConfItem,
                wcRadConfSalesman,
                wcRadConfClientSelect,
                wcRadConfItemSelect,
                wcRadConfSalesmanSelect,
                wcRadConfSalesmanSelectReport,
                wcDaysToShow,
                wcSoftwareConfigured,
                wcDeleteOrderAfterDownload,
                wcBackwardCompatibility,
                wcCompanyData,
                wcDeskBlackTheme,
                wcLogo,
                wcRequestLot,
                wcRequestPresentation,
                wcBarcodeLabelDefault
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getAllPreSale(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                //WAREHOUSE COUNTER DATA
                psAutoSend,
                psClient,
                psClientMultiSelect,
                psClientSelect,
                psDaysToShow,
                psImageControlDocument,
                psSyncQtyRegistry,
                psItem,
                psItemSelect,
                psLastUpdate,
                psLastUpdateClient,
                psLastUpdateCountry,
                psLastUpdateItem,
                psLastUpdateItemCategory,
                psLastUpdateOrder,
                psLastUpdatePayment,
                psLastUpdateReturn,
                psLastUpdateRoute,
                psLastUpdatePromotion,
                psLastUpdateSalesmanRouteLog,
                psLastUpdateSalesman,
                psMinCharCountSearch,
                psOrderMain,
                psPaymentMain,
                psReturnMain,
                psSalesmanRouteLogResumeMain,
                psSoftwareConfigured,
                psInterval,
                psSyncOnEveryRequest,
                psSyncThreads,
                psMaxSimulThreads,
                psPingTimeOut
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getAllAssetControl(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                // ASSET CONTROL DATA
                acAutoSend,
                acLastUpdate,
                acLastUpdateAsset,
                acLastUpdateAssetMaintenance,
                acLastUpdateAssetMaintenanceLog,
                acLastUpdateAssetReview,
                acLastUpdateCostCentre,
                acLastUpdateAssetMaintenanceProgramed,
                acLastUpdateItemCategory,
                acLastUpdateMaintenanceType,
                acLastUpdateMaintenanceTypeGroup,
                acLastUpdateProviders,
                acLastUpdateRepairman,
                acLastUpdateRepairshop,
                acLastUpdateUser,
                acLastUpdateWarehouse,
                acLastUpdateWarehouseArea,
                acLastUpdateWarehouseMovement,
                acLastUpdateAttribute,
                acLastUpdateAttributeCategory,
                acLastUpdateRoute,
                acLastUpdateDataCollectionRule,
                acLastUpdateDataCollection,
                acLastUpdateRouteProcess,
                acLastUpdateDataCollectionReport,
                acLastUpdateWarehouseMovementContent,
                acLastUpdateAssetReviewContent,
                acLastUpdateDataCollectionContent,
                acLastUpdateRouteProcessContent,
                acLastUpdateBarcodeLabelCustom,
                acLastUpdateUserWarehouseArea,

                // AC appdata
                acMaxSimulThreads,
                acPingTimeOut,
                acInterval,
                acDaysToShow,
                acMaxActionLogs,
                acSoftwareConfigured,
                acAddLabelNumberOnBarcode,
                acDownloadDbStartup,

                // REPORT data
                acReportAssetAmortization,
                acReportAssetDischarge,
                acReportAssetLease,
                acReportAssetRental,
                acReportAssetReview,
                acReportAssetReviewComp,
                acReportAssetStatus,
                acReportAssetUnsubscribe,
                acReportWarehouseMovement,
                acMaintenanceDays,
                acSyncQtyRegistry,

                // Control data
                acControlAsset,
                acControlUser,
                acControlProvider,
                acControlAssetLabelPrint,
                acControlAssetMonitoringMainForm,
                acControlWarehouseMovementMainForm,
                acControlAssetReviewComp,
                acControlWarehouseMovementReviewComp,
                acControlAssetReviewMainForm,
                acControlAssetReviewSelectComp,
                acControlActionLogMainForm,
                acControlWarehouseMovementContent,
                acControlWarehouseMovementSelect,
                acControlAssetAmortization,
                acControlAssetReviewContent,
                acControlAssetMonitoring,
                acControlAssetSelect,
                acControlAssetRental,
                acControlAssetLease,
                acControlWarehouseMovementContentSpecial,
                acControlWarehouseMovementContentReport,
                acControlAssetReviewContentSpecial,
                acControlAssetMaintenanceProgramed,
                acControlMaintenanceTypeSelect,
                acControlAssetMovements,
                acControlImageControlDocumentMainForm,
                acControlRoute,
                acControlDataCollectionRule,
                acControlDataCollection,
                acControlDataCollectionContent,
                acControlDataCollectionMainForm,
                acControlRouteProcessMainForm,
                acControlRouteProcessContent,
                acControlDataCollectionReport,
                acControlDataCollectionReportMainForm,
                acControlErrorLogMainForm,
                acControlBarcodeLabelCustom
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getAllImageControl(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                // IMAGE CONTROL DATA
                wsIcConnectTimeout,
                wsIcConnectionLifetime,
                wsIcDB,
                wsIcDefaultCommandTimeout,
                wsIcMaxBufferSize,
                wsIcMaxReceivedMessageSize,
                wsIcPass,
                wsIcConnectionLifetime,
                wsIcConnectTimeout,
                wsIcPort,
                wsIcProxyDomain,
                wsIcProxyPass,
                wsIcProxyPort,
                wsIcProxyUrl,
                wsIcProxyUse,
                wsIcProxyUseCredentials,
                wsIcProxyUseDefaultCredentials,
                wsIcProxyUseDefaultProxy,
                wsIcProxyUser,
                wsIcSkipPing,
                wsIcUrl,
                wsIcUser,
                icUser,
                icPass
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getAllAssetControlMaintenance(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                // ASSETCONTROL MANTEINANCE DATA
                acmControlActionLogMainForm,
                acmControlAsset,
                acmControlAssetInGeneral,
                acmControlAssetMaintenanceMainForm,
                acmControlAssetMaintenanceProgramed,
                acmControlAssetOnRepairshopMainForm,
                acmControlAssetSelect,
                acmControlAssetWithMaintenanceProgramedMainForm,
                acmControlMaintenanceTypeSelect,
                acmControlRepairman,
                acmControlAssetReceptionContentSpecial,
                acmControlAssetRemissionContentSpecial,
                acmControlWarehouseMovementContent,
                acmControlWarehouseMovementSelect,
                acmControlAssetMaintenanceLog,
                acmReportAssetMaintenance,
                acmReportWarehouseMovement,
                acmReportAssetMaintenanceProgramed,
                acmImageControlDocument,
                acmMaxSimulThreads,
                acmLastUpdate,
                acmInterval,
                acmPingTimeOut,
                acmLastUpdateAsset,
                acmLastUpdateAssetMaintenance,
                acmLastUpdateAssetMaintenanceLog,
                acmLastUpdateItemCategory,
                acmLastUpdateMaintenanceType,
                acmLastUpdateMaintenanceTypeGroup,
                acmLastUpdateProviders,
                acmLastUpdateRepairman,
                acmLastUpdateRepairshop,
                acmLastUpdateUser,
                acmLastUpdateWarehouse,
                acmLastUpdateWarehouseArea,
                acmLastUpdateWarehouseMovement,
                acmLastUpdateWarehouseMovementContent,
                acmDownloadDbStartup,
                acmManteinanceDays,
                acmDaysToShow,
                acmMaxActionLogs,
                acmSoftwareConfigured,
                acmAddLabelNumberOnBarcode,
                acmSyncQtyRegistry,
                acmControlErrorLogMainForm
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getAllMedidorMovil(): ArrayList<ConfEntry> {
            val allSections = ArrayList<ConfEntry>()
            Collections.addAll(
                allSections,
                // MEDIDOR MOVIL
                mmDaysToShow,
                mmDeleteOrderAfterDownload,
                mmLastUpdate,
                mmRadGridClient,
                mmRadGridClientSelect,
                mmRadGridMeter,
                mmRadGridMeterMain,
                mmRadGridMeterSelect,
                mmRadGridReadingMain,
                mmRadGridRoute,
                mmRadGridStreet,
                mmRadGridUser,
                mmRadGridUserSelect,
                mmRadGridFtpFileSelect,
                mmSeparatorCharacter,
                mmSoftwareConfigured,
                mmLogo,
                mmDeskBlackTheme,
                mmCompanyData,
                mmRadGridCity,
                mmRadGridState
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confEntryId }))
        }

        fun getByConfSection(confSection: ConfSection): ConfEntry? {
            return getAllGeneral().firstOrNull { it.confSection == confSection }
        }

        fun getById(confEntryId: Long): ConfEntry? {
            return getAllGeneral().firstOrNull { it.confEntryId == confEntryId }
        }
    }
}