package com.example.assetControl.data.room.repository.maintenance

import com.example.assetControl.data.room.dao.maintenance.MaintenanceTypeGroupDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.maintenance.MaintenanceTypeGroup
import com.example.assetControl.data.room.entity.maintenance.MaintenanceTypeGroupEntity
import kotlinx.coroutines.runBlocking

class MaintenanceTypeGroupRepository {
    private val dao: MaintenanceTypeGroupDao
        get() = database.maintenanceTypeGroupDao()

    fun sync(typeGroup: MaintenanceTypeGroup) = runBlocking {
        dao.insert(MaintenanceTypeGroupEntity(typeGroup))
    }
}
