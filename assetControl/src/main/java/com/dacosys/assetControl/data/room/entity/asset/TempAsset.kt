package com.dacosys.assetControl.data.room.entity.asset

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.asset.TempAsset.Entry

@Entity(tableName = Entry.TABLE_NAME)
data class TempAsset(
    @PrimaryKey @ColumnInfo(name = Entry.TEMP_ID) val tempId: Long
) {
    object Entry {
        private const val TEMP_PREFIX = "temp_"

        const val TABLE_NAME = "${TEMP_PREFIX}_asset"
        const val TEMP_ID = "${TEMP_PREFIX}_id"
    }
}


