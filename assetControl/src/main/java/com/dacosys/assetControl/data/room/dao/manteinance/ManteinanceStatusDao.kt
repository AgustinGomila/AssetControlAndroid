package com.dacosys.assetControl.data.room.dao.manteinance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceStatus
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceStatus.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface ManteinanceStatusDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllManteinanceStatus(): Flow<List<ManteinanceStatus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManteinanceStatus(manteinanceStatus: ManteinanceStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(manteinanceStatusList: List<ManteinanceStatus>)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}