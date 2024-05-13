package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseMovementContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouseMovementContent(content: WarehouseMovementContent)

    @Update
    suspend fun updateWarehouseMovementContent(content: WarehouseMovementContent)

    @Delete
    suspend fun deleteWarehouseMovementContent(content: WarehouseMovementContent)

    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllWarehouseMovementContents(): Flow<List<WarehouseMovementContent>>
}

