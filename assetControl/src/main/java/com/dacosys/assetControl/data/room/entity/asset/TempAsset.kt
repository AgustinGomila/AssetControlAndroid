package com.dacosys.assetControl.data.room.entity.asset

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.asset.TempAsset.TempAssetEntry

@Entity(tableName = TempAssetEntry.TABLE_NAME)
data class TempAsset(
    @PrimaryKey
    @ColumnInfo(name = TempAssetEntry.TEMP_ID)
    val tempId: Long
) {
    object TempAssetEntry {
        const val TABLE_NAME = "temp_asset"
        const val TEMP_ID = "temp__id"
    }
}


