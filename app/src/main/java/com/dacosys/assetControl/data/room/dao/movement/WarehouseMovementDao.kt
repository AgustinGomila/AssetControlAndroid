package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.location.WarehouseAreaEntry
import com.dacosys.assetControl.data.room.dto.location.WarehouseEntry
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovementEntry
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementEntity
import java.util.*

@Dao
interface WarehouseMovementDao {
    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseMovementEntry.TABLE_NAME}.${WarehouseMovementEntry.TRANSFERRED_DATE} IS NULL " +
                "AND ${WarehouseMovementEntry.TABLE_NAME}.${WarehouseMovementEntry.COMPLETED} = 1"
    )
    suspend fun selectByNoTransferred(): List<WarehouseMovement>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warehouseMovement: WarehouseMovementEntity): Long


    @Update
    suspend fun update(warehouseMovement: WarehouseMovementEntity)

    @Query(
        "UPDATE ${WarehouseMovementEntry.TABLE_NAME} SET ${WarehouseMovementEntry.ORIGIN_WAREHOUSE_AREA_ID} = :newValue " +
                "WHERE ${WarehouseMovementEntry.ORIGIN_WAREHOUSE_AREA_ID} = :oldValue"
    )
    suspend fun updateOriginWarehouseAreaId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${WarehouseMovementEntry.TABLE_NAME} SET ${WarehouseMovementEntry.DESTINATION_WAREHOUSE_AREA_ID} = :newValue " +
                "WHERE ${WarehouseMovementEntry.DESTINATION_WAREHOUSE_AREA_ID} = :oldValue"
    )
    suspend fun updateDestinationWarehouseAreaId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${WarehouseMovementEntry.TABLE_NAME} SET ${WarehouseMovementEntry.ORIGIN_WAREHOUSE_ID} = :newValue " +
                "WHERE ${WarehouseMovementEntry.ORIGIN_WAREHOUSE_ID} = :oldValue"
    )
    suspend fun updateOriginWarehouseId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${WarehouseMovementEntry.TABLE_NAME} SET ${WarehouseMovementEntry.DESTINATION_WAREHOUSE_ID} = :newValue " +
                "WHERE ${WarehouseMovementEntry.DESTINATION_WAREHOUSE_ID} = :oldValue"
    )
    suspend fun updateDestinationWarehouseId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${WarehouseMovementEntry.TABLE_NAME} SET ${WarehouseMovementEntry.ID} = :newValue, " +
                "${WarehouseMovementEntry.TRANSFERRED_DATE} = :date " +
                "WHERE ${WarehouseMovementEntry.ID} = :oldValue"
    )
    suspend fun updateId(newValue: Long, oldValue: Long, date: Date)


    @Query("DELETE $BASIC_FROM WHERE ${WarehouseMovementEntry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE $BASIC_FROM WHERE ${WarehouseMovementEntry.TRANSFERRED_DATE} IS NOT NULL")
    suspend fun deleteTransferred()


    companion object {
        const val BASIC_SELECT = "SELECT ${WarehouseMovementEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${WarehouseMovementEntry.TABLE_NAME}"

        private const val ORIG_PREFIX = "orig"
        private const val DEST_PREFIX = "dest"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${WarehouseAreaEntry.TABLE_NAME} AS ${ORIG_PREFIX}_${WarehouseAreaEntry.TABLE_NAME} " +
                    "ON ${ORIG_PREFIX}_${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} = ${WarehouseMovementEntry.TABLE_NAME}.${WarehouseMovementEntry.ORIGIN_WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${WarehouseEntry.TABLE_NAME} AS ${ORIG_PREFIX}_${WarehouseEntry.TABLE_NAME} " +
                    "ON ${ORIG_PREFIX}_${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = ${ORIG_PREFIX}_${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.WAREHOUSE_ID} " +
                    "LEFT JOIN ${WarehouseAreaEntry.TABLE_NAME} AS ${DEST_PREFIX}_${WarehouseAreaEntry.TABLE_NAME} " +
                    "ON ${DEST_PREFIX}_${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} = ${WarehouseMovementEntry.TABLE_NAME}.${WarehouseMovementEntry.DESTINATION_WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${WarehouseEntry.TABLE_NAME} AS ${DEST_PREFIX}_${WarehouseEntry.TABLE_NAME} " +
                    "ON ${DEST_PREFIX}_${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = ${DEST_PREFIX}_${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.WAREHOUSE_ID} "


        const val BASIC_JOIN_FIELDS =
            "${DEST_PREFIX}_${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${WarehouseMovementEntry.DESTINATION_WAREHOUSE_STR}," +
                    "${DEST_PREFIX}_${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} AS ${WarehouseMovementEntry.DESTINATION_WAREHOUSE_AREA_STR}," +
                    "${ORIG_PREFIX}_${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${WarehouseMovementEntry.ORIGIN_WAREHOUSE_STR}," +
                    "${ORIG_PREFIX}_${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} AS ${WarehouseMovementEntry.ORIGIN_WAREHOUSE_AREA_STR}"
    }
}
