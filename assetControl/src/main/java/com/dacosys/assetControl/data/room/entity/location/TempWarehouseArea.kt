package com.dacosys.assetControl.data.room.entity.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.location.TempWarehouseArea.Entry

@Entity(tableName = Entry.TABLE_NAME)
data class TempWarehouseArea(
    @PrimaryKey @ColumnInfo(name = Entry.TEMP_ID) val tempId: Long
) {
    object Entry {
        const val TEMP_PREFIX = "temp_"

        const val TABLE_NAME = "${TEMP_PREFIX}_warehouse_area"
        const val TEMP_ID = "${TEMP_PREFIX}_id"
    }
}


