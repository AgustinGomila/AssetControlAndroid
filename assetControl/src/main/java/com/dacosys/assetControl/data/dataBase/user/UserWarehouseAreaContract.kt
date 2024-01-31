package com.dacosys.assetControl.data.dataBase.user

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object UserWarehouseAreaContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            UserWarehouseAreaEntry.USER_ID,
            UserWarehouseAreaEntry.WAREHOUSE_AREA_ID,
            UserWarehouseAreaEntry.SEE,
            UserWarehouseAreaEntry.MOVE,
            UserWarehouseAreaEntry.COUNT,
            UserWarehouseAreaEntry.CHECK
        )
    }

    abstract class UserWarehouseAreaEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "user_warehouse_area"

            const val WAREHOUSE_AREA_ID = "warehouse_area_id"
            const val USER_ID = "user_id"
            const val SEE = "see"
            const val MOVE = "move"
            const val COUNT = "count"
            const val CHECK = "check"
        }
    }
}
