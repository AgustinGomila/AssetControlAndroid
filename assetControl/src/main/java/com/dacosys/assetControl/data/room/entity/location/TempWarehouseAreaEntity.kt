package com.dacosys.assetControl.data.room.entity.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.location.TempWarehouseAreaEntity.Entry

@Entity(tableName = Entry.TABLE_NAME)
data class TempWarehouseAreaEntity(
    @PrimaryKey @ColumnInfo(name = Entry.TEMP_ID) val tempId: Long
) {
    object Entry {
        private const val TEMP_PREFIX = "temp"

        private const val WAREHOUSE_AREA = "warehouse_area"
        private const val ID = "id"

        const val TABLE_NAME = "${TEMP_PREFIX}_${WAREHOUSE_AREA}"
        const val TEMP_ID = "${TEMP_PREFIX}_${ID}"


    }
}


