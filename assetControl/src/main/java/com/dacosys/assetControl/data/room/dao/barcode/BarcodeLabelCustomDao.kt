package com.dacosys.assetControl.data.room.dao.barcode

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.barcode.BarcodeLabelCustom
import com.dacosys.assetControl.data.room.dto.barcode.BarcodeLabelCustom.Entry
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelCustomEntity

@Dao
interface BarcodeLabelCustomDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id $BASIC_ORDER")
    suspend fun selectById(id: Long): BarcodeLabelCustom?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.BARCODE_LABEL_TARGET_ID} = :targetId $BASIC_ORDER")
    suspend fun selectByBarcodeLabelTargetId(targetId: Int): List<BarcodeLabelCustom>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.BARCODE_LABEL_TARGET_ID} = :targetId AND ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 $BASIC_ORDER")
    suspend fun selectByBarcodeLabelTargetIdActive(targetId: Int): List<BarcodeLabelCustom>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(label: BarcodeLabelCustomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(labels: List<BarcodeLabelCustomEntity>)

    @Transaction
    suspend fun insert(entities: List<BarcodeLabelCustom>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(BarcodeLabelCustomEntity(entity))
            completedTask(index + 1)
        }
    }


    @Query("DELETE $BASIC_FROM WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DESCRIPTION}"
    }
}