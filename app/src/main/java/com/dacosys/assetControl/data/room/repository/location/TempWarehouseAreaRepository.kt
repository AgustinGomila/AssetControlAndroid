package com.dacosys.assetControl.data.room.repository.location


import com.dacosys.assetControl.data.room.dao.location.TempWarehouseAreaDao
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.location.TempWarehouseAreaEntity

import kotlinx.coroutines.runBlocking

class TempWarehouseAreaRepository {
    private val dao: TempWarehouseAreaDao
        get() = database.tempWarehouseAreaDao()

    fun select() = runBlocking { dao.select() }

    fun insert(ids: List<Long>) = runBlocking {
        val areas = ids.map { TempWarehouseAreaEntity(it) }.toList()
        dao.insert(areas)
    }

    fun deleteAll() = runBlocking {
        dao.deleteAll()
    }
}