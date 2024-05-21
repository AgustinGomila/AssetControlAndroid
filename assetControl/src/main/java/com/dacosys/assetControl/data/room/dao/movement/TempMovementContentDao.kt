package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.movement.TempMovementContentEntity
import com.dacosys.assetControl.data.room.entity.movement.TempMovementContentEntity.Entry

@Dao
interface TempMovementContentDao {
    @Query("SELECT MAX(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.WAREHOUSE_MOVEMENT_ID} = :wmId $BASIC_ORDER")
    suspend fun selectByTempIds(wmId: Long): List<TempMovementContentEntity>


    @Insert
    suspend fun insert(content: TempMovementContentEntity)

    @Insert
    suspend fun insert(contents: List<TempMovementContentEntity>)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DESCRIPTION}, " +
                "${Entry.TABLE_NAME}.${Entry.CODE}, " +
                "${Entry.TABLE_NAME}.${Entry.ASSET_ID}"
    }
}