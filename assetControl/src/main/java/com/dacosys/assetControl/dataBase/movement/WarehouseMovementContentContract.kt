package com.dacosys.assetControl.dataBase.movement

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

    abstract class WarehouseMovementContentEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "warehouse_movement_content"

            const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
            const val WAREHOUSE_MOVEMENT_CONTENT_ID = "_id"
            const val ASSET_ID = "asset_id"
            const val CODE = "code"
            const val QTY = "qty"
        }
    }
}
