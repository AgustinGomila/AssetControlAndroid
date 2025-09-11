package com.example.assetControl.data.room.dao.barcode

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.assetControl.data.room.dto.barcode.BarcodeLabelCustom
import com.example.assetControl.data.room.dto.barcode.BarcodeLabelCustomEntry
import com.example.assetControl.data.room.entity.barcode.BarcodeLabelCustomEntity

@Dao
interface BarcodeLabelCustomDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${BarcodeLabelCustomEntry.TABLE_NAME}.${BarcodeLabelCustomEntry.ID} = :id $BASIC_ORDER")
    suspend fun selectById(id: Long): BarcodeLabelCustom?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${BarcodeLabelCustomEntry.TABLE_NAME}.${BarcodeLabelCustomEntry.BARCODE_LABEL_TARGET_ID} = :targetId $BASIC_ORDER")
    suspend fun selectByBarcodeLabelTargetId(targetId: Int): List<BarcodeLabelCustom>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${BarcodeLabelCustomEntry.TABLE_NAME}.${BarcodeLabelCustomEntry.BARCODE_LABEL_TARGET_ID} = :targetId AND ${BarcodeLabelCustomEntry.TABLE_NAME}.${BarcodeLabelCustomEntry.ACTIVE} = 1 $BASIC_ORDER")
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


    @Query("DELETE $BASIC_FROM WHERE ${BarcodeLabelCustomEntry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${BarcodeLabelCustomEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${BarcodeLabelCustomEntry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${BarcodeLabelCustomEntry.TABLE_NAME}.${BarcodeLabelCustomEntry.DESCRIPTION}"
    }
}