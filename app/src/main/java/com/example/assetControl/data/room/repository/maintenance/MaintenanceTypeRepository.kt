package com.example.assetControl.data.room.repository.maintenance

import com.example.assetControl.data.room.dao.maintenance.MaintenanceTypeDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.maintenance.MaintenanceType
import com.example.assetControl.data.room.entity.maintenance.MaintenanceTypeEntity
import kotlinx.coroutines.runBlocking

class MaintenanceTypeRepository {
    private val dao: MaintenanceTypeDao
        get() = database.maintenanceTypeDao()

    fun getAll(): List<MaintenanceType> = runBlocking { dao.select() }


    fun sync(type: MaintenanceType) = runBlocking {
        dao.insert(MaintenanceTypeEntity(type))
    }
}