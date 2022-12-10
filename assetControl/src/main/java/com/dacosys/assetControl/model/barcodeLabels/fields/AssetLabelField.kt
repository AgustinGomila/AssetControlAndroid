package com.dacosys.assetControl.model.barcodeLabels.fields

import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.configuration.entries.ConfEntry
import java.util.*

class AssetLabelField(asset: Asset, forReport: Boolean) {
    var asset: Asset? = null
    private var forReport: Boolean = false

    init {
        this.asset = asset
        this.forReport = forReport
    }

    fun getField(): ArrayList<BarcodeLabelField> {
        var tempCode = ""
        if (asset != null) {
            val addLabelNumber = Statics.prefsGetBoolean(ConfEntry.acAddLabelNumberOnBarcode)
            tempCode = if (addLabelNumber) {
                if (forReport) {
                    asset!!.code + Statics.reservedChar + "0"
                } else {
                    if (asset!!.labelNumber == null) {
                        asset!!.code + Statics.reservedChar + "1"
                    } else {
                        asset!!.code + Statics.reservedChar + (asset!!.labelNumber!! + 1).toString()
                    }
                }
            } else {
                asset!!.code
            }
        }

        val labelField = ArrayList<BarcodeLabelField>()
        Collections.addAll(
            labelField,
            BarcodeLabelField(DefinedField.ASSET_ID, asset!!.assetId.toString()),
            BarcodeLabelField(DefinedField.CODE, tempCode),
            BarcodeLabelField(
                DefinedField.EAN, if (asset!!.ean == null) {
                    ""
                } else {
                    asset!!.ean!!
                }
            ),
            BarcodeLabelField(DefinedField.DESCRIPTION, asset!!.description, 38),
            //BarcodeLabelField(DefinedField.PRICE, asset!!.Price.ToString()),
            BarcodeLabelField(DefinedField.ASSET_WAREHOUSE, asset!!.warehouseStr),
            BarcodeLabelField(DefinedField.ASSET_WAREHOUSE_AREA, asset!!.warehouseAreaStr),
            BarcodeLabelField(DefinedField.ORIGINAL_WAREHOUSE, asset!!.originalWarehouseStr),
            BarcodeLabelField(
                DefinedField.ORIGINAL_WAREHOUSE_AREA,
                asset!!.originalWarehouseAreaStr
            ),
            BarcodeLabelField(
                DefinedField.MANUFACTURER, if (asset!!.manufacturer == null) {
                    ""
                } else {
                    asset!!.manufacturer!!
                }, 15
            ),
            BarcodeLabelField(
                DefinedField.MODEL, if (asset!!.model == null) {
                    ""
                } else {
                    asset!!.model!!
                }, 15
            ),
            BarcodeLabelField(
                DefinedField.SERIAL_NUMBER, if (asset!!.serialNumber == null) {
                    ""
                } else {
                    asset!!.serialNumber!!
                }
            ),
            //BarcodeLabelField(DefinedField.WARRANTY_DUE, asset!!.WarrantyDue.ToString()),
            //BarcodeLabelField(DefinedField.OBS, asset!!.Observations),
            BarcodeLabelField(
                DefinedField.OWNERSHIP,
                asset!!.ownershipStatus!!.description
            ),
            BarcodeLabelField(
                DefinedField.STATUS,
                asset!!.assetStatus!!.description
            ),
            BarcodeLabelField(DefinedField.ITEM_CATEGORY, asset!!.itemCategoryStr),
            //BarcodeLabelField(DefinedField.PROVIDER, (asset!!.Provider != null) ? asset!!.Provider.Name : string.Empty),
            BarcodeLabelField(DefinedField.ASSET_DATE_PRINTED, Calendar.DATE.toString(), 34)
        )

        return labelField
    }
}