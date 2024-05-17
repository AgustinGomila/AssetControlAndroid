package com.dacosys.assetControl.data.room.repository.maintenance

import com.dacosys.assetControl.data.enums.maintenance.MaintenanceStatus
import com.dacosys.assetControl.data.room.dao.maintenance.MaintenanceStatusDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import kotlinx.coroutines.runBlocking
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceStatus as MaintenanceStatusRoom

class MaintenanceStatusRepository {
    private val dao: MaintenanceStatusDao
        get() = database.maintenanceStatusDao()

    fun sync() = runBlocking {
        val status = MaintenanceStatus.getAll().map { MaintenanceStatusRoom(it) }.toList()
        dao.deleteAll()
        dao.insert(status)
    }
}