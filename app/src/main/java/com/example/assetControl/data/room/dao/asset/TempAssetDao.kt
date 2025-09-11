package com.example.assetControl.data.room.dao.asset

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assetControl.data.room.entity.asset.TempAssetEntity
import com.example.assetControl.data.room.entity.asset.TempAssetEntity.Entry

@Dao
interface TempAssetDao {
    @Query("$BASIC_SELECT $BASIC_FROM")
    suspend fun select(): List<TempAssetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<TempAssetEntity>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}