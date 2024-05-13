package com.dacosys.assetControl.data.room.dao.asset

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.dacosys.assetControl.data.room.entity.asset.TempAsset

@Dao
interface TempAssetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tempAsset: TempAsset)

    @Delete
    suspend fun delete(tempAsset: TempAsset)

}