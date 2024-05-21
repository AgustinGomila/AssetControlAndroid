package com.dacosys.assetControl.data.room.entity.asset

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.asset.TempAssetEntity.Entry

@Entity(tableName = Entry.TABLE_NAME)
data class TempAssetEntity(
    @PrimaryKey @ColumnInfo(name = Entry.TEMP_ID) val tempId: Long
) {
    object Entry {
        private const val TEMP_PREFIX = "temp"

        private const val ASSET = "asset"
        private const val ID = "id"

        const val TABLE_NAME = "${TEMP_PREFIX}_${ASSET}"
        const val TEMP_ID = "${TEMP_PREFIX}_${ID}"
    }
}


