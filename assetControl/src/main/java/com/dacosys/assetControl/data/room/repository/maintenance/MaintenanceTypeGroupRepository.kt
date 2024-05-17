package com.dacosys.assetControl.data.room.repository.maintenance

import com.dacosys.assetControl.data.room.dao.maintenance.MaintenanceTypeGroupDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceTypeGroup
import kotlinx.coroutines.runBlocking

class MaintenanceTypeGroupRepository {
    private val dao: MaintenanceTypeGroupDao
        get() = database.maintenanceTypeGroupDao()

    fun insert(typeGroup: MaintenanceTypeGroup) = runBlocking {
        dao.insert(typeGroup)
    }

    fun update(typeGroup: MaintenanceTypeGroup): Boolean {
        val r = runBlocking {
            dao.update(typeGroup)
            true
        }
        return r
    }
}
