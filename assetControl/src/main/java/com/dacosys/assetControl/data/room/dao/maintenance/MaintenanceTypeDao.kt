package com.dacosys.assetControl.data.room.dao.maintenance

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceType
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceType.Entry

@Dao
interface MaintenanceTypeDao {
    @Query("$BASIC_SELECT $BASIC_FROM")
    suspend fun select(): List<MaintenanceType>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(type: MaintenanceType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(typeList: List<MaintenanceType>)


    @Update
    suspend fun update(type: MaintenanceType)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}