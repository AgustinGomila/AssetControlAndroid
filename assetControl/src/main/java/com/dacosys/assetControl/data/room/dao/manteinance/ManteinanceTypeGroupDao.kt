package com.dacosys.assetControl.data.room.dao.manteinance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceTypeGroup
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceTypeGroup.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface ManteinanceTypeGroupDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllManteinanceTypeGroups(): Flow<List<ManteinanceTypeGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManteinanceTypeGroup(manteinanceTypeGroup: ManteinanceTypeGroup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(manteinanceTypeGroups: List<ManteinanceTypeGroup>)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}
