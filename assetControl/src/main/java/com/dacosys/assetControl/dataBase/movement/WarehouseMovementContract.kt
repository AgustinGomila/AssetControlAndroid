package com.dacosys.assetControl.dataBase.movement

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object WarehouseMovementContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            WarehouseMovementEntry.WAREHOUSE_MOVEMENT_ID,
            WarehouseMovementEntry.WAREHOUSE_MOVEMENT_DATE,
            WarehouseMovementEntry.OBS,
            WarehouseMovementEntry.USER_ID,
            WarehouseMovementEntry.ORIGIN_WAREHOUSE_AREA_ID,
            WarehouseMovementEntry.ORIGIN_WAREHOUSE_ID,
            WarehouseMovementEntry.DESTINATION_WAREHOUSE_AREA_ID,
            WarehouseMovementEntry.DESTINATION_WAREHOUSE_ID,
            WarehouseMovementEntry.TRANSFERED_DATE,
            WarehouseMovementEntry.COMPLETED,
            WarehouseMovementEntry.COLLECTOR_WAREHOUSE_MOVEMENT_ID
        )
    }

    abstract class WarehouseMovementEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "warehouse_movement"
            const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
            const val WAREHOUSE_MOVEMENT_DATE = "warehouse_movement_date"
            const val OBS = "obs"
            const val USER_ID = "user_id"
            const val ORIGIN_WAREHOUSE_AREA_ID = "origin_warehouse_area_id"
            const val ORIGIN_WAREHOUSE_ID = "origin_warehouse_id"
            const val DESTINATION_WAREHOUSE_AREA_ID = "destination_warehouse_area_id"
            const val DESTINATION_WAREHOUSE_ID = "destination_warehouse_id"
            const val TRANSFERED_DATE = "transfered_date"
            const val COMPLETED = "completed"
            const val COLLECTOR_WAREHOUSE_MOVEMENT_ID = "_id"

            const val ORIGIN_WAREHOUSE_AREA_STR = "origin_warehouse_area_str"
            const val ORIGIN_WAREHOUSE_STR = "origin_warehouse_str"
            const val DESTINATION_WAREHOUSE_AREA_STR = "destination_warehouse_area_str"
            const val DESTINATION_WAREHOUSE_STR = "destination_warehouse_str"
        }
    }
}
