package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.movement.TempMovementContentEntity
import com.dacosys.assetControl.data.room.entity.movement.TempMovementContentEntry

@Dao
interface TempMovementContentDao {
    @Query("SELECT MAX(${TempMovementContentEntry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${TempMovementContentEntry.TABLE_NAME}.${TempMovementContentEntry.WAREHOUSE_MOVEMENT_ID} = :wmId $BASIC_ORDER")
    suspend fun selectByTempIds(wmId: Long): List<TempMovementContentEntity>


    @Insert
    suspend fun insert(content: TempMovementContentEntity)

    @Insert
    suspend fun insert(contents: List<TempMovementContentEntity>)

    @Query("DELETE FROM ${TempMovementContentEntry.TABLE_NAME}")
    suspend fun deleteAll()

    companion object {
        const val BASIC_SELECT = "SELECT ${TempMovementContentEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${TempMovementContentEntry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${TempMovementContentEntry.TABLE_NAME}.${TempMovementContentEntry.DESCRIPTION}, " +
                    "${TempMovementContentEntry.TABLE_NAME}.${TempMovementContentEntry.CODE}, " +
                    "${TempMovementContentEntry.TABLE_NAME}.${TempMovementContentEntry.ASSET_ID}"
    }
}