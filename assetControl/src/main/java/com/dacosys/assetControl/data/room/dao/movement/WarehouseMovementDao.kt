package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.location.Warehouse
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement.Entry
import java.util.*

@Dao
interface WarehouseMovementDao {
    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED_DATE} IS NULL " +
                "AND ${Entry.TABLE_NAME}.${Entry.COMPLETED} = 1"
    )
    suspend fun selectByNoTransferred(): List<WarehouseMovement>

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
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"

        private val wEntry = Warehouse.Entry
        private val waEntry = WarehouseArea.Entry

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${wEntry.TABLE_NAME} AS orig_${wEntry.TABLE_NAME} ON orig_${wEntry.TABLE_NAME}.${wEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ORIGIN_WAREHOUSE_ID} " +
                    "LEFT JOIN ${waEntry.TABLE_NAME} AS orig_${waEntry.TABLE_NAME} ON orig_${waEntry.TABLE_NAME}.${waEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ORIGIN_WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${wEntry.TABLE_NAME} AS dest_${wEntry.TABLE_NAME} ON dest_${wEntry.TABLE_NAME}.${wEntry.ID} = ${Entry.TABLE_NAME}.${Entry.DESTINATION_WAREHOUSE_ID} " +
                    "LEFT JOIN ${waEntry.TABLE_NAME} AS dest_${waEntry.TABLE_NAME} ON dest_${waEntry.TABLE_NAME}.${waEntry.ID} = ${Entry.TABLE_NAME}.${Entry.DESTINATION_WAREHOUSE_AREA_ID} "

        const val BASIC_JOIN_FIELDS =
            "dest_${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.DESTINATION_WAREHOUSE_STR}," +
                    "dest_${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.DESTINATION_WAREHOUSE_AREA_STR}," +
                    "orig_${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.ORIGIN_WAREHOUSE_STR}," +
                    "orig_${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.ORIGIN_WAREHOUSE_AREA_STR}"
    }
}
