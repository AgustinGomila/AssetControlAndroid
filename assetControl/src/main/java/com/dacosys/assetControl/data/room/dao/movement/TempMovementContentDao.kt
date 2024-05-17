package com.dacosys.assetControl.data.room.dao.movement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.movement.TempMovementContent
import com.dacosys.assetControl.data.room.entity.movement.TempMovementContent.Entry

@Dao
interface TempMovementContentDao {
    @Query("SELECT MAX(${Entry.WAREHOUSE_MOVEMENT_CONTENT_ID}) $BASIC_FROM")
    fun selectMaxId(): Long?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.WAREHOUSE_MOVEMENT_ID} = :wmId $BASIC_ORDER")
    fun selectByTempIds(wmId: Long): List<TempMovementContent>


    @Insert
    suspend fun insert(content: TempMovementContent)

    @Insert
    suspend fun insert(contents: List<TempMovementContent>)

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
