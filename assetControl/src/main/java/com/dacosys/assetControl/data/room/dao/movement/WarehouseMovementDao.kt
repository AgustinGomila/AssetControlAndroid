package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement.Entry

@Dao
interface WarehouseMovementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warehouseMovement: WarehouseMovement)

    @Update
    suspend fun update(warehouseMovement: WarehouseMovement)

    @Delete
    suspend fun delete(warehouseMovement: WarehouseMovement)

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    fun getWarehouseMovementById(id: Long): WarehouseMovement?
}
