package com.dacosys.assetControl.data.room.dao.asset

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.asset.TempAsset
import com.dacosys.assetControl.data.room.entity.asset.TempAsset.Entry

@Dao
interface TempAssetDao {
    @Query("$BASIC_SELECT $BASIC_FROM")
    suspend fun select(): List<TempAsset>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<TempAsset>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}