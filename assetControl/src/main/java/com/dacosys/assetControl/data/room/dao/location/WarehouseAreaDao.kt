package com.dacosys.assetControl.data.room.dao.location

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseAreaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouseArea(warehouseArea: WarehouseArea)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWarehouseAreas(warehouseAreas: List<WarehouseArea>)

    @Update
    suspend fun updateWarehouseArea(warehouseArea: WarehouseArea)

    @Delete
    suspend fun deleteWarehouseArea(warehouseArea: WarehouseArea)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAllWarehouseAreas()

    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllWarehouseAreas(): Flow<List<WarehouseArea>>
}