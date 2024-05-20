package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement.Entry
import java.util.*

@Dao
interface WarehouseMovementDao {
    @Query(
        "$BASIC_SELECT $BASIC_FROM " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED_DATE} IS NULL " +
                "AND ${Entry.TABLE_NAME}.${Entry.COMPLETED} = 1"
    )
    fun selectByNoTransferred(): List<WarehouseMovement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warehouseMovement: WarehouseMovement)

    @Update
    suspend fun update(warehouseMovement: WarehouseMovement)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ORIGIN_WAREHOUSE_AREA_ID} = :newValue WHERE ${Entry.ORIGIN_WAREHOUSE_AREA_ID} = :oldValue")
    suspend fun updateOriginWarehouseAreaId(oldValue: Long, newValue: Long)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.DESTINATION_WAREHOUSE_AREA_ID} = :newValue WHERE ${Entry.DESTINATION_WAREHOUSE_AREA_ID} = :oldValue")
    suspend fun updateDestinationWarehouseAreaId(oldValue: Long, newValue: Long)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ORIGIN_WAREHOUSE_ID} = :newValue WHERE ${Entry.ORIGIN_WAREHOUSE_ID} = :oldValue")
    suspend fun updateOriginWarehouseId(oldValue: Long, newValue: Long)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.DESTINATION_WAREHOUSE_ID} = :newValue WHERE ${Entry.DESTINATION_WAREHOUSE_ID} = :oldValue")
    suspend fun updateDestinationWarehouseId(oldValue: Long, newValue: Long)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue, ${Entry.TRANSFERRED_DATE} = :date WHERE ${Entry.ID} = :oldValue")
    suspend fun updateId(oldValue: Long, newValue: Long, date: Date = Date())


    @Query("DELETE $BASIC_FROM WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE $BASIC_FROM WHERE ${Entry.TRANSFERRED_DATE} IS NOT NULL")
    suspend fun deleteTransferred()


    companion object {
        const val BASIC_SELECT = "SELECT *"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}
