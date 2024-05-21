package com.dacosys.assetControl.data.room.dao.location

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.location.TempWarehouseAreaEntity
import com.dacosys.assetControl.data.room.entity.location.TempWarehouseAreaEntity.Entry

@Dao
interface TempWarehouseAreaDao {
    @Query("$BASIC_SELECT $BASIC_FROM")
    suspend fun select(): List<TempWarehouseAreaEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(areas: List<TempWarehouseAreaEntity>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}