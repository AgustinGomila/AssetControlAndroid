package com.dacosys.assetControl.data.room.dao.location

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.location.Warehouse
import com.dacosys.assetControl.data.room.dto.location.Warehouse.Entry
import com.dacosys.assetControl.data.room.entity.location.WarehouseEntity

@Dao
interface WarehouseDao {
    @Query("$BASIC_SELECT $BASIC_FROM $BASIC_ORDER")
    suspend fun select(): List<Warehouse>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.ACTIVE} = 1 $BASIC_ORDER")
    suspend fun selectActive(): List<Warehouse>

    @Query("SELECT MIN(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id")
    suspend fun selectById(id: Long): Warehouse?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TRANSFERRED} = 0 $BASIC_ORDER")
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

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue WHERE ${Entry.ID} = :oldValue")
    suspend fun updateId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} " +
                "SET ${Entry.TRANSFERRED} = 1 " +
                "WHERE ${Entry.ID} IN (:ids)"
    )
    suspend fun updateTransferred(
        ids: Array<Long>
    )


    @Delete
    suspend fun delete(warehouse: WarehouseEntity)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DESCRIPTION}"
    }
}