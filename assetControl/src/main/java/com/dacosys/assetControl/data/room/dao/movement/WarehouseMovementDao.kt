package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.location.Warehouse
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovement.Entry
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementEntity
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
    suspend fun insert(warehouseMovement: WarehouseMovementEntity): Long


    @Update
    suspend fun update(warehouseMovement: WarehouseMovementEntity)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.ORIGIN_WAREHOUSE_AREA_ID} = :newValue " +
                "WHERE ${Entry.ORIGIN_WAREHOUSE_AREA_ID} = :oldValue"
    )
    suspend fun updateOriginWarehouseAreaId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.DESTINATION_WAREHOUSE_AREA_ID} = :newValue " +
                "WHERE ${Entry.DESTINATION_WAREHOUSE_AREA_ID} = :oldValue"
    )
    suspend fun updateDestinationWarehouseAreaId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.ORIGIN_WAREHOUSE_ID} = :newValue " +
                "WHERE ${Entry.ORIGIN_WAREHOUSE_ID} = :oldValue"
    )
    suspend fun updateOriginWarehouseId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.DESTINATION_WAREHOUSE_ID} = :newValue " +
                "WHERE ${Entry.DESTINATION_WAREHOUSE_ID} = :oldValue"
    )
    suspend fun updateDestinationWarehouseId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue, " +
                "${Entry.TRANSFERRED_DATE} = :date " +
                "WHERE ${Entry.ID} = :oldValue"
    )
    suspend fun updateId(newValue: Long, oldValue: Long, date: Date)


    @Query("DELETE $BASIC_FROM WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE $BASIC_FROM WHERE ${Entry.TRANSFERRED_DATE} IS NOT NULL")
    suspend fun deleteTransferred()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"

        private val wEntry = Warehouse.Entry
        private val waEntry = WarehouseArea.Entry

        private const val ORIG_PREFIX = "orig"
        private const val DEST_PREFIX = "dest"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${waEntry.TABLE_NAME} AS ${ORIG_PREFIX}_${waEntry.TABLE_NAME} " +
                    "ON ${ORIG_PREFIX}_${waEntry.TABLE_NAME}.${waEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ORIGIN_WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${wEntry.TABLE_NAME} AS ${ORIG_PREFIX}_${wEntry.TABLE_NAME} " +
                    "ON ${ORIG_PREFIX}_${wEntry.TABLE_NAME}.${wEntry.ID} = ${ORIG_PREFIX}_${waEntry.TABLE_NAME}.${waEntry.WAREHOUSE_ID} " +
                    "LEFT JOIN ${waEntry.TABLE_NAME} AS ${DEST_PREFIX}_${waEntry.TABLE_NAME} " +
                    "ON ${DEST_PREFIX}_${waEntry.TABLE_NAME}.${waEntry.ID} = ${Entry.TABLE_NAME}.${Entry.DESTINATION_WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${wEntry.TABLE_NAME} AS ${DEST_PREFIX}_${wEntry.TABLE_NAME} " +
                    "ON ${DEST_PREFIX}_${wEntry.TABLE_NAME}.${wEntry.ID} = ${DEST_PREFIX}_${waEntry.TABLE_NAME}.${waEntry.WAREHOUSE_ID} "


        const val BASIC_JOIN_FIELDS =
            "${DEST_PREFIX}_${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.DESTINATION_WAREHOUSE_STR}," +
                    "${DEST_PREFIX}_${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} AS ${Entry.DESTINATION_WAREHOUSE_AREA_STR}," +
                    "${ORIG_PREFIX}_${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.ORIGIN_WAREHOUSE_STR}," +
                    "${ORIG_PREFIX}_${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} AS ${Entry.ORIGIN_WAREHOUSE_AREA_STR}"
    }
}
