package com.example.assetControl.data.room.dao.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.dto.location.WarehouseAreaEntry
import com.example.assetControl.data.room.dto.location.WarehouseEntry
import com.example.assetControl.data.room.entity.location.WarehouseAreaEntity

@Dao
interface WarehouseAreaDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<WarehouseArea>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectActive(): List<WarehouseArea>

    @Query("SELECT MIN(${WarehouseAreaEntry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} = :id"
    )
    suspend fun selectById(id: Long): WarehouseArea?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.TRANSFERRED} = 0 $BASIC_ORDER"
    )
    suspend fun selectNoTransferred(): List<WarehouseArea>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} LIKE '%' || :wDescription || '%'  " +
                "AND ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} LIKE '%' || :waDescription || '%'  " +
                BASIC_ORDER
    )
    suspend fun selectByDescription(wDescription: String, waDescription: String): List<WarehouseArea>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} LIKE '%' || :wDescription || '%'  " +
                "AND ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} LIKE '%' || :waDescription || '%'  " +
                "AND ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ACTIVE} = 1 " +
                BASIC_ORDER
    )
    suspend fun selectByDescriptionActive(wDescription: String, waDescription: String): List<WarehouseArea>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} IN (:ids) " +
                BASIC_ORDER
    )
    suspend fun selectByTempIds(ids: List<Long>): List<WarehouseArea>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warehouseArea: WarehouseAreaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(areas: List<WarehouseAreaEntity>)

    @Transaction
    suspend fun insert(entities: List<WarehouseArea>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(WarehouseAreaEntity(entity))
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(area: WarehouseAreaEntity)

    @Query("UPDATE ${WarehouseAreaEntry.TABLE_NAME} SET ${WarehouseAreaEntry.WAREHOUSE_ID} = :newValue WHERE ${WarehouseAreaEntry.WAREHOUSE_ID} = :oldValue")
    suspend fun updateWarehouseId(newValue: Long, oldValue: Long)

    @Query("UPDATE ${WarehouseAreaEntry.TABLE_NAME} SET ${WarehouseAreaEntry.ID} = :newValue WHERE ${WarehouseAreaEntry.ID} = :oldValue")
    suspend fun updateWarehouseAreaId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${WarehouseAreaEntry.TABLE_NAME} " +
                "SET ${WarehouseAreaEntry.TRANSFERRED} = 1 " +
                "WHERE ${WarehouseAreaEntry.ID} IN (:ids)"
    )
    suspend fun updateTransferred(
        ids: Array<Long>
    )


    @Delete
    suspend fun delete(warehouseArea: WarehouseAreaEntity)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${WarehouseAreaEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${WarehouseAreaEntry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION}, " +
                "${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${WarehouseEntry.TABLE_NAME} ON ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.WAREHOUSE_ID}"
        const val BASIC_JOIN_FIELDS =
            "${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${WarehouseAreaEntry.WAREHOUSE_STR}"
    }
}