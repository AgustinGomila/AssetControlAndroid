package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.enums.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.dao.route.RouteProcessStatusDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStatusEntity
import kotlinx.coroutines.runBlocking

class RouteProcessStatusRepository {
    private val dao: RouteProcessStatusDao
        get() = database.routeProcessStatusDao()

    fun sync() = runBlocking {
        val status = RouteProcessStatus.getAll().map { RouteProcessStatusEntity(it) }
        dao.deleteAll()
        dao.insert(status)
    }
}