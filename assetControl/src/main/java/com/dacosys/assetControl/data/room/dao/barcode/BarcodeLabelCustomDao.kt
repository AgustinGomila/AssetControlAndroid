package com.dacosys.assetControl.data.room.dao.barcode

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelCustom
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelCustom.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeLabelCustomDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllBarcodeLabels(): Flow<List<BarcodeLabelCustom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarcodeLabel(barcodeLabel: BarcodeLabelCustom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(barcodeLabels: List<BarcodeLabelCustom>)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}