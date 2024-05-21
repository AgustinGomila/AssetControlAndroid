package com.dacosys.assetControl.data.room.dao.maintenance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.dto.maintenance.MaintenanceStatus.Entry
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceStatusEntity

@Dao
interface MaintenanceStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statuses: List<MaintenanceStatusEntity>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    companion object {
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}