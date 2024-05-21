package com.dacosys.assetControl.data.room.dao.location

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.location.Warehouse
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea.Entry
import com.dacosys.assetControl.data.room.entity.location.WarehouseAreaEntity

@Dao
interface WarehouseAreaDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<WarehouseArea>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectActive(): List<WarehouseArea>

    @Query("SELECT MIN(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id"
    )
    suspend fun selectById(id: Long): WarehouseArea?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED} = 0 $BASIC_ORDER"
    )
    suspend fun selectNoTransferred(): List<WarehouseArea>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :wDescription || '%'  " +
                "AND ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :waDescription || '%'  " +
                BASIC_ORDER
    )
    suspend fun selectByDescription(wDescription: String, waDescription: String): List<WarehouseArea>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :wDescription || '%'  " +
                "AND ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :waDescription || '%'  " +
                "AND ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 " +
                BASIC_ORDER
    )
    suspend fun selectByDescriptionActive(wDescription: String, waDescription: String): List<WarehouseArea>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ID} IN (:ids) " +
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

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.WAREHOUSE_ID} = :newValue WHERE ${Entry.WAREHOUSE_ID} = :oldValue")
    suspend fun updateWarehouseId(oldValue: Long, newValue: Long)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue WHERE ${Entry.ID} = :oldValue")
    suspend fun updateWarehouseAreaId(oldValue: Long, newValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} " +
                "SET ${Entry.TRANSFERRED} = 1 " +
                "WHERE ${Entry.ID} IN (:ids)"
    )
    suspend fun updateTransferred(
        ids: Array<Long>
    )


    @Delete
    suspend fun delete(warehouseArea: WarehouseAreaEntity)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        private val wEntry = Warehouse.Entry

        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DESCRIPTION}, " +
                "${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${wEntry.TABLE_NAME} ON ${wEntry.TABLE_NAME}.${wEntry.ID} = ${Entry.TABLE_NAME}.${Entry.WAREHOUSE_ID}"
        const val BASIC_JOIN_FIELDS = "${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_STR}"
    }
}