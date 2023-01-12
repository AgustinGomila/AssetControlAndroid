package com.dacosys.assetControl.dataBase.location

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object WarehouseContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            WarehouseEntry.WAREHOUSE_ID,
            WarehouseEntry.DESCRIPTION,
            WarehouseEntry.ACTIVE,
            WarehouseEntry.TRANSFERRED
        )
    }

    abstract class WarehouseEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "warehouse"

            const val WAREHOUSE_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
            const val TRANSFERRED = "transferred"
        }
    }
}
