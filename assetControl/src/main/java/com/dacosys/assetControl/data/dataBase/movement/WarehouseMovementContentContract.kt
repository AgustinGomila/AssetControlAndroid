package com.dacosys.assetControl.data.dataBase.movement

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object WarehouseMovementContentContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID,
            WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_CONTENT_ID,
            WarehouseMovementContentEntry.ASSET_ID,
            WarehouseMovementContentEntry.CODE,
            WarehouseMovementContentEntry.QTY
        )
    }

    fun getAllTempColumns(): Array<String> {
        return arrayOf(
            WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID,
            WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_CONTENT_ID,
            WarehouseMovementContentEntry.CONTENT_STATUS,
            WarehouseMovementContentEntry.ASSET_ID,
            WarehouseMovementContentEntry.CODE,
            WarehouseMovementContentEntry.QTY,
            WarehouseMovementContentEntry.DESCRIPTION,
            WarehouseMovementContentEntry.WAREHOUSE_AREA_ID,
            WarehouseMovementContentEntry.OWNERSHIP_STATUS,
            WarehouseMovementContentEntry.STATUS,
            WarehouseMovementContentEntry.ITEM_CATEGORY_ID,
            WarehouseMovementContentEntry.LABEL_NUMBER,
            WarehouseMovementContentEntry.MANUFACTURER,
            WarehouseMovementContentEntry.MODEL,
            WarehouseMovementContentEntry.SERIAL_NUMBER,
            WarehouseMovementContentEntry.PARENT_ID,
            WarehouseMovementContentEntry.EAN,
            WarehouseMovementContentEntry.ITEM_CATEGORY_STR,
            WarehouseMovementContentEntry.WAREHOUSE_STR,
            WarehouseMovementContentEntry.WAREHOUSE_AREA_STR
        )
    }

    abstract class WarehouseMovementContentEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "warehouse_movement_content"

            const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
            const val WAREHOUSE_MOVEMENT_CONTENT_ID = "_id"
            const val CONTENT_STATUS = "content_status"
            const val ASSET_ID = "asset_id"
            const val CODE = "code"
            const val QTY = "qty"

            // Tabla temporal
            const val DESCRIPTION = "description"
            const val WAREHOUSE_AREA_ID = "warehouse_area_id"
            const val OWNERSHIP_STATUS = "ownership_status"
            const val STATUS = "status"
            const val ITEM_CATEGORY_ID = "item_category_id"
            const val LABEL_NUMBER = "label_number"
            const val MANUFACTURER = "manufacturer"
            const val MODEL = "model"
            const val SERIAL_NUMBER = "serial_number"
            const val PARENT_ID = "parent_id"
            const val EAN = "ean"
            const val ITEM_CATEGORY_STR = "item_category_str"
            const val WAREHOUSE_STR = "warehouse_str"
            const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        }
    }
}
