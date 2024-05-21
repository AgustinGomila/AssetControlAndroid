package com.dacosys.assetControl.data.room.dao.barcode

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.dto.barcode.BarcodeLabelTarget.Entry
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelTargetEntity

@Dao
interface BarcodeLabelTargetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statuses: List<BarcodeLabelTargetEntity>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    companion object {
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}