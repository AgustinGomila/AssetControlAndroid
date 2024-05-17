package com.dacosys.assetControl.data.model.barcodeFields

import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import java.util.*

class WarehouseAreaLabelField(warehouseArea: WarehouseArea) {
    var warehouseArea: WarehouseArea? = null

    init {
        this.warehouseArea = warehouseArea
    }

    fun getField(): ArrayList<BarcodeLabelField> {
        val labelField = ArrayList<BarcodeLabelField>()
        val wa = warehouseArea
        if (wa != null) {
            val waId = "#WA#${(wa.id).toString().padStart(5, '0')}#"

            Collections.addAll(
                labelField,
                BarcodeLabelField(DefinedField.WAREHOUSE, wa.warehouseStr),
                BarcodeLabelField(DefinedField.WAREHOUSE_AREA, wa.description),
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