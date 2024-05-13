package com.dacosys.assetControl.data.room.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.entity.user.UserWarehouseArea.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.USER_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"),
        Index(value = [Entry.WAREHOUSE_AREA_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}")
    ]
)
data class UserWarehouseArea(
    @ColumnInfo(name = Entry.USER_ID) val userId: Long,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long,
    @ColumnInfo(name = Entry.SEE) val see: Int,
    @ColumnInfo(name = Entry.MOVE) val move: Int,
    @ColumnInfo(name = Entry.COUNT) val count: Int,
    @ColumnInfo(name = Entry.CHECK) val check: Int
) {
    object Entry {
        const val TABLE_NAME = "user_warehouse_area"
        const val USER_ID = "user_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val SEE = "see"
        const val MOVE = "move"
        const val COUNT = "count"
        const val CHECK = "check"
    }
}
