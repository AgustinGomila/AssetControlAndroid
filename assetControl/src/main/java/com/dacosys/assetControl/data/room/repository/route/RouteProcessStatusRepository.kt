package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.enums.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.dao.route.RouteProcessStatusDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import kotlinx.coroutines.runBlocking
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStatus as RouteProcessStatusRoom

class RouteProcessStatusRepository {
    private val dao: RouteProcessStatusDao
        get() = database.routeProcessStatusDao()

    fun sync() = runBlocking {
        val status = RouteProcessStatus.getAll().map { RouteProcessStatusRoom(it) }.toList()
        dao.deleteAll()
        dao.insert(status)
    }
}