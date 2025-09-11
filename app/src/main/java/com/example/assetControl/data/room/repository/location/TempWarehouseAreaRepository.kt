package com.example.assetControl.data.room.repository.location


import com.example.assetControl.data.room.dao.location.TempWarehouseAreaDao
import com.example.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.example.assetControl.data.room.entity.location.TempWarehouseAreaEntity

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