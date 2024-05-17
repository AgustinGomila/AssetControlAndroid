package com.dacosys.assetControl.data.room.dao.maintenance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceStatus
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceStatus.Entry

@Dao
interface MaintenanceStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statuses: List<MaintenanceStatus>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    companion object {
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}