package com.dacosys.assetControl.data.room.repository.maintenance

import com.dacosys.assetControl.data.room.dao.maintenance.MaintenanceTypeDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceType
import kotlinx.coroutines.runBlocking

class MaintenanceTypeRepository {
    private val dao: MaintenanceTypeDao
        get() = database.maintenanceTypeDao()

    fun getAll(): List<MaintenanceType> = runBlocking { dao.select() }


    fun insert(type: MaintenanceType) = runBlocking {
        dao.insert(type)
    }


    fun update(type: MaintenanceType): Boolean {
        val r = runBlocking {
            dao.update(type)
            true
        }
        return r
    }
}