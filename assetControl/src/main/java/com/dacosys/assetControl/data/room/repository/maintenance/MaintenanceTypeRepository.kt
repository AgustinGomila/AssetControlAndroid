package com.dacosys.assetControl.data.room.repository.maintenance

import com.dacosys.assetControl.data.room.dao.maintenance.MaintenanceTypeDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.maintenance.MaintenanceType
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceTypeEntity
import kotlinx.coroutines.runBlocking

class MaintenanceTypeRepository {
    private val dao: MaintenanceTypeDao
        get() = database.maintenanceTypeDao()

    fun getAll(): List<MaintenanceType> = runBlocking { dao.select() }


    fun insert(type: MaintenanceType) = runBlocking {
        dao.insert(MaintenanceTypeEntity(type))
    }


    fun update(type: MaintenanceType): Boolean {
        val r = runBlocking {
            dao.update(MaintenanceTypeEntity(type))
            true
        }
        return r
    }
}