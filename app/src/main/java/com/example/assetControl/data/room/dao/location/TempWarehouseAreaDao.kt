package com.example.assetControl.data.room.dao.location

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assetControl.data.room.entity.location.TempWarehouseAreaEntity
import com.example.assetControl.data.room.entity.location.TempWarehouseAreaEntry

@Dao
interface TempWarehouseAreaDao {
    @Query("$BASIC_SELECT $BASIC_FROM")
    suspend fun select(): List<TempWarehouseAreaEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(areas: List<TempWarehouseAreaEntity>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${TempWarehouseAreaEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${TempWarehouseAreaEntry.TABLE_NAME}"
    }
}