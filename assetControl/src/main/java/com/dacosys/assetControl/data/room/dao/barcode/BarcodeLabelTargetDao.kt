package com.dacosys.assetControl.data.room.dao.barcode

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelTarget
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelTarget.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeLabelTargetDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllBarcodeLabelTargets(): Flow<List<BarcodeLabelTarget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarcodeLabelTarget(barcodeLabelTarget: BarcodeLabelTarget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(barcodeLabelTargets: List<BarcodeLabelTarget>)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}