package com.dacosys.assetControl.model.barcodeLabels.fields

import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import java.util.*

class WarehouseAreaLabelField(warehouseArea: WarehouseArea) {
    var warehouseArea: WarehouseArea? = null

    init {
        this.warehouseArea = warehouseArea
    }

    fun getField(): ArrayList<BarcodeLabelField> {
        val labelField = ArrayList<BarcodeLabelField>()
        if (warehouseArea != null) {
            val waId = "#WA#${(warehouseArea!!.warehouseAreaId).toString().padStart(5, '0')}#"

            Collections.addAll(
                labelField,
                BarcodeLabelField(DefinedField.WAREHOUSE, warehouseArea!!.warehouse!!.description),
                BarcodeLabelField(DefinedField.WAREHOUSE_AREA, warehouseArea!!.description),
                BarcodeLabelField(DefinedField.WAREHOUSE_AREA_ID, waId),
                BarcodeLabelField(
                    DefinedField.WAREHOUSE_AREA_DATE_PRINTED,
                    Calendar.DATE.toString(),
                    34
                )
            )
        }
        return labelField
    }
}