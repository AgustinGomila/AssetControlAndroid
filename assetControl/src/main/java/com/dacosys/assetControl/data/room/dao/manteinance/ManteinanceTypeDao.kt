package com.dacosys.assetControl.data.room.dao.manteinance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceType
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceType.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface ManteinanceTypeDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllManteinanceTypes(): Flow<List<ManteinanceType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManteinanceType(manteinanceType: ManteinanceType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(manteinanceTypeList: List<ManteinanceType>)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}