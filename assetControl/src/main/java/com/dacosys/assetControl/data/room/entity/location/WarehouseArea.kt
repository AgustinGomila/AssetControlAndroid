package com.dacosys.assetControl.data.room.entity.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"),
        Index(value = [Entry.WAREHOUSE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}")
    ]
)
data class WarehouseArea(
    @PrimaryKey
    @ColumnInfo(name = Entry.COLUMN_ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) val warehouseId: Long,
    @ColumnInfo(name = Entry.TRANSFERRED) val transferred: Int?
) {
    object Entry {
        const val TABLE_NAME = "warehouse_area"
        const val COLUMN_ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val WAREHOUSE_ID = "warehouse_id"
        const val TRANSFERRED = "transferred"
    }
}

