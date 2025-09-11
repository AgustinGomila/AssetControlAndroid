package com.example.assetControl.data.room.dao.maintenance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.assetControl.data.room.entity.maintenance.MaintenanceTypeGroupEntity

@Dao
interface MaintenanceTypeGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(typeGroup: MaintenanceTypeGroupEntity)
}
