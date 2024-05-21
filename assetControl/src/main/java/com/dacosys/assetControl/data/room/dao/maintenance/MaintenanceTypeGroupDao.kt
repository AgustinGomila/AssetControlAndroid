package com.dacosys.assetControl.data.room.dao.maintenance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceTypeGroupEntity

@Dao
interface MaintenanceTypeGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(typeGroup: MaintenanceTypeGroupEntity)

    @Update
    suspend fun update(typeGroup: MaintenanceTypeGroupEntity)
}
