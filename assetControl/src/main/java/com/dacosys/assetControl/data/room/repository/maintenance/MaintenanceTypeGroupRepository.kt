package com.dacosys.assetControl.data.room.repository.maintenance

import com.dacosys.assetControl.data.room.dao.maintenance.MaintenanceTypeGroupDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.maintenance.MaintenanceTypeGroup
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceTypeGroupEntity
import kotlinx.coroutines.runBlocking

class MaintenanceTypeGroupRepository {
    private val dao: MaintenanceTypeGroupDao
        get() = database.maintenanceTypeGroupDao()

    fun insert(typeGroup: MaintenanceTypeGroup) = runBlocking {
        dao.insert(MaintenanceTypeGroupEntity(typeGroup))
    }

    fun update(typeGroup: MaintenanceTypeGroup): Boolean {
        val r = runBlocking {
            dao.update(MaintenanceTypeGroupEntity(typeGroup))
            true
        }
        return r
    }
}
