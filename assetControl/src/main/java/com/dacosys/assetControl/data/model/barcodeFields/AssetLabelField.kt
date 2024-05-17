package com.dacosys.assetControl.data.model.barcodeFields

import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.settings.entries.ConfEntry
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import java.util.*

class AssetLabelField(asset: Asset, forReport: Boolean) {
    private var mAsset: Asset? = null
    private var forReport: Boolean = false

    init {
        this.mAsset = asset
        this.forReport = forReport
    }

    fun getField(): ArrayList<BarcodeLabelField> {
        val labelField = ArrayList<BarcodeLabelField>()

        val tempCode: String
        val asset = mAsset

        if (asset != null) {
            val addLabelNumber = prefsGetBoolean(ConfEntry.acAddLabelNumberOnBarcode)

            tempCode =
                if (addLabelNumber) {
                    if (forReport) {
                        asset.code + Statics.RESERVED_CHAR + "0"
                    } else {
                        if (asset.labelNumber == null) {
                            asset.code + Statics.RESERVED_CHAR + "1"
                        } else {
                            asset.code + Statics.RESERVED_CHAR + ((asset.labelNumber ?: 0) + 1).toString()
                        }
                    }
                } else {
                    asset.code
                }

            Collections.addAll(
                labelField,
                BarcodeLabelField(DefinedField.ASSET_ID, asset.id.toString()),
                BarcodeLabelField(DefinedField.CODE, tempCode),
                BarcodeLabelField(DefinedField.EAN, asset.ean ?: ""),
                BarcodeLabelField(DefinedField.DESCRIPTION, asset.description, 38),
                BarcodeLabelField(DefinedField.ASSET_WAREHOUSE, asset.warehouseStr),
                BarcodeLabelField(DefinedField.ASSET_WAREHOUSE_AREA, asset.warehouseAreaStr),
                BarcodeLabelField(DefinedField.ORIGINAL_WAREHOUSE, asset.originalWarehouseStr),
                BarcodeLabelField(DefinedField.ORIGINAL_WAREHOUSE_AREA, asset.originalWarehouseAreaStr),
                BarcodeLabelField(DefinedField.MANUFACTURER, asset.manufacturer ?: "", 15),
                BarcodeLabelField(DefinedField.MODEL, asset.model ?: "", 15),
                BarcodeLabelField(DefinedField.SERIAL_NUMBER, asset.serialNumber ?: ""),
                BarcodeLabelField(DefinedField.OWNERSHIP, asset.ownership.description),
                BarcodeLabelField(DefinedField.STATUS, asset.assetStatus.description),
                BarcodeLabelField(DefinedField.ITEM_CATEGORY, asset.itemCategoryStr),
                BarcodeLabelField(DefinedField.ASSET_DATE_PRINTED, Calendar.DATE.toString(), 34)
            )
        }

        return labelField
    }
}