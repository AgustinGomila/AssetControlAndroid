package com.example.assetControl.data.room.dao.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.assetControl.data.room.dto.location.Warehouse
import com.example.assetControl.data.room.dto.location.WarehouseEntry
import com.example.assetControl.data.room.entity.location.WarehouseEntity

@Dao
interface WarehouseDao {
    @Query("$BASIC_SELECT $BASIC_FROM $BASIC_ORDER")
    suspend fun select(): List<Warehouse>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${WarehouseEntry.ACTIVE} = 1 $BASIC_ORDER")
    suspend fun selectActive(): List<Warehouse>

    @Query("SELECT MIN(${WarehouseEntry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = :id")
    suspend fun selectById(id: Long): Warehouse?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${WarehouseEntry.TRANSFERRED} = 0 $BASIC_ORDER")
    suspend fun selectNoTransferred(): List<Warehouse>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warehouse: WarehouseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warehouses: List<WarehouseEntity>)

    @Transaction
    suspend fun insert(entities: List<Warehouse>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(WarehouseEntity(entity))
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(warehouse: WarehouseEntity)

    @Query("UPDATE ${WarehouseEntry.TABLE_NAME} SET ${WarehouseEntry.ID} = :newValue WHERE ${WarehouseEntry.ID} = :oldValue")
    suspend fun updateId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${WarehouseEntry.TABLE_NAME} " +
                "SET ${WarehouseEntry.TRANSFERRED} = 1 " +
                "WHERE ${WarehouseEntry.ID} IN (:ids)"
    )
    suspend fun updateTransferred(
        ids: Array<Long>
    )


    @Delete
    suspend fun delete(warehouse: WarehouseEntity)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${WarehouseEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${WarehouseEntry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION}"
    }
}