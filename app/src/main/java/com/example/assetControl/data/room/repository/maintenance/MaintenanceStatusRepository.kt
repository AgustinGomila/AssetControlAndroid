package com.example.assetControl.data.room.repository.maintenance

import com.example.assetControl.data.enums.maintenance.MaintenanceStatus
import com.example.assetControl.data.room.dao.maintenance.MaintenanceStatusDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.entity.maintenance.MaintenanceStatusEntity
import kotlinx.coroutines.runBlocking

class MaintenanceStatusRepository {
    private val dao: MaintenanceStatusDao
        get() = database.maintenanceStatusDao()

    fun sync() = runBlocking {
        val status = MaintenanceStatus.getAll().map { MaintenanceStatusEntity(it) }
        dao.deleteAll()
        dao.insert(status)
    }
}