package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent.Entry

@Dao
interface WarehouseMovementContentDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.WAREHOUSE_MOVEMENT_ID} = :id")
    fun selectByWarehouseMovementId(id: Long): List<WarehouseMovementContent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: WarehouseMovementContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contents: List<WarehouseMovementContent>)

    @Transaction
    suspend fun insert(entities: List<WarehouseMovementContent>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(entity)
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(content: WarehouseMovementContent)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ASSET_ID} = :newValue WHERE ${Entry.ASSET_ID} = :oldValue")
    suspend fun updateAssetId(oldValue: Long, newValue: Long)


    @Delete
    suspend fun delete(content: WarehouseMovementContent)

    @Query(
        "DELETE $BASIC_FROM WHERE ${Entry.WAREHOUSE_MOVEMENT_ID} " +
                "IN (SELECT ${wmEntry.TABLE_NAME}.${wmEntry.ID} " +
                "FROM ${wmEntry.TABLE_NAME} " +
                "WHERE ${wmEntry.TABLE_NAME}.${wmEntry.TRANSFERED_DATE} IS NOT NULL )"
    )
    suspend fun deleteTransferred()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.ID}"

        private val wmEntry = WarehouseMovement.Entry
    }
}

