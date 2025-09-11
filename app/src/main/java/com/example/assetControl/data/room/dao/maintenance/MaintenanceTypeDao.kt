package com.example.assetControl.data.room.dao.maintenance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assetControl.data.room.dto.maintenance.MaintenanceType
import com.example.assetControl.data.room.dto.maintenance.MaintenanceTypeEntry
import com.example.assetControl.data.room.entity.maintenance.MaintenanceTypeEntity

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
        const val BASIC_SELECT = "SELECT ${MaintenanceTypeEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${MaintenanceTypeEntry.TABLE_NAME}"
    }
}