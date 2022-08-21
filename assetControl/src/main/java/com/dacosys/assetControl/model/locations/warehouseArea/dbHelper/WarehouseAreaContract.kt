package com.dacosys.assetControl.model.locations.warehouseArea.dbHelper

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object WarehouseAreaContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            WarehouseAreaEntry.WAREHOUSE_AREA_ID,
            WarehouseAreaEntry.DESCRIPTION,
            WarehouseAreaEntry.ACTIVE,
            WarehouseAreaEntry.WAREHOUSE_ID,
            WarehouseAreaEntry.TRANSFERRED
        )
    }

    abstract class WarehouseAreaEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "warehouse_area"

            const val WAREHOUSE_AREA_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
            const val WAREHOUSE_ID = "warehouse_id"
            const val WAREHOUSE_STR = "warehouse_str"
            const val TRANSFERRED = "transferred"
        }
    }
}
