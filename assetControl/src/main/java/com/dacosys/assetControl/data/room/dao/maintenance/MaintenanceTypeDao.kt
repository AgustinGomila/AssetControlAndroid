package com.dacosys.assetControl.data.room.dao.maintenance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.dto.maintenance.MaintenanceType
import com.dacosys.assetControl.data.room.dto.maintenance.MaintenanceType.Entry
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceTypeEntity

@Dao
interface MaintenanceTypeDao {
    @Query("$BASIC_SELECT $BASIC_FROM")
    suspend fun select(): List<MaintenanceType>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(type: MaintenanceTypeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(typeList: List<MaintenanceTypeEntity>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}