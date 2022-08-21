package com.dacosys.assetControl.utils.configuration.entries

import java.util.*

/**
 * Created by Agustin on 16/01/2017.
 */

class ConfSection(confSectionId: Long, description: String) {
    var confSectionId: Long = 0
    var description: String = ""

    init {
        this.description = description
        this.confSectionId = confSectionId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is ConfSection) {
            false
        } else this.confSectionId == other.confSectionId
    }

    override fun hashCode(): Int {
        return this.confSectionId.hashCode()
    }

    class CustomComparator : Comparator<ConfSection> {
        override fun compare(o1: ConfSection, o2: ConfSection): Int {
            if (o1.confSectionId < o2.confSectionId) {
                return -1
            } else if (o1.confSectionId > o2.confSectionId) {
                return 1
            }
            return 0
        }
    }

    companion object {
        var appData = ConfSection(
            1,
            "AplicationData"
        )
        var collData = ConfSection(
            2,
            "CollectorData"
        )
        var webserviceData = ConfSection(
            3,
            "WebServiceData"
        )
        var mySqlConnectionData = ConfSection(
            4,
            "MySqlConnectionData"
        )
        var proxyWebServiceData = ConfSection(
            5,
            "ProxyWebServiceData"
        )
        private var proxyMySqlData = ConfSection(
            6,
            "ProxyMySqlData"
        )
        var licenseData = ConfSection(
            7,
            "LicenseData"
        )
        var warehouseCounterData = ConfSection(
            8,
            "WarehouseCounterData"
        )
        var assetControlData = ConfSection(
            9,
            "AssetControlData"
        )
        var webservice2Data = ConfSection(
            10,
            "WebService2Data"
        )
        var proxyWebService2Data = ConfSection(
            11,
            "ProxyWebService2Data"
        )
        var printerData = ConfSection(
            11,
            "PrinterData"
        )
        var assetControlMantData = ConfSection(
            12,
            "AssetControlMantData"
        )
        var presaleData = ConfSection(
            13,
            "PresaleData"
        )
        var medidorMovilData = ConfSection(
            14,
            "MedidorMovilData"
        )
        var ftpData = ConfSection(
            15,
            "FTPData"
        )
        var mySqlConnection2Data = ConfSection(
            16,
            "MySqlConnection2Data"
        )
        var imageControlData = ConfSection(
            17,
            "ImageControlData"
        )
        var languageData = ConfSection(
            18,
            "languageData"
        )
        private var pickingControlData = ConfSection(
            19,
            "PickingControlData"
        )
        var ldapData = ConfSection(
            20,
            "LDAPData"
        )
        var dataGridCEData = ConfSection(
            21,
            "DataGridCEData"
        )

        fun getAll(): ArrayList<ConfSection> {
            val allSections = ArrayList<ConfSection>()
            Collections.addAll(
                allSections,
                appData,
                collData,
                webserviceData,
                mySqlConnectionData,
                proxyMySqlData,
                proxyWebServiceData,
                licenseData,
                warehouseCounterData,
                assetControlData,
                webservice2Data,
                proxyWebService2Data,
                printerData,
                assetControlMantData,
                presaleData,
                medidorMovilData,
                ftpData,
                mySqlConnection2Data,
                imageControlData,
                languageData,
                pickingControlData,
                ldapData,
                dataGridCEData
            )

            return ArrayList(allSections.sortedWith(compareBy { it.confSectionId }))
        }

        fun getById(mainButtonId: Long): ConfSection? {
            return getAll().firstOrNull { it.confSectionId == mainButtonId }
        }
    }
}