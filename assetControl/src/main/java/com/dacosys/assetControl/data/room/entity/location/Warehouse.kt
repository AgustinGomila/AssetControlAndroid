package com.dacosys.assetControl.data.room.entity.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.location.Warehouse.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"),
    ]
)
data class Warehouse(
    @PrimaryKey
    @ColumnInfo(name = Entry.COLUMN_ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.TRANSFERRED) val transferred: Int?
) {
    object Entry {
        const val TABLE_NAME = "warehouse"
        const val COLUMN_ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val TRANSFERRED = "transferred"
    }
}