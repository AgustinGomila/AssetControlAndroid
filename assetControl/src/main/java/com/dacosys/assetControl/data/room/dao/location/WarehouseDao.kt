package com.dacosys.assetControl.data.room.dao.location

import androidx.room.*
import com.dacosys.assetControl.data.model.location.Warehouse
import com.dacosys.assetControl.data.room.entity.location.Warehouse.Entry
import kotlinx.coroutines.flow.Flow


@Dao
interface WarehouseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: Warehouse)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWarehouses(warehouses: List<Warehouse>)

    @Update
    suspend fun updateWarehouse(warehouse: Warehouse)

    @Delete
    suspend fun deleteWarehouse(warehouse: Warehouse)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAllWarehouses()

    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllWarehouses(): Flow<List<Warehouse>>
}

