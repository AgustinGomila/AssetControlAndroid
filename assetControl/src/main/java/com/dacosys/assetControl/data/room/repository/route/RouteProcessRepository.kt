package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.room.dao.route.RouteProcessDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.RouteProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RouteProcessRepository {
    private val routeProcessDao: RouteProcessDao by lazy {
        database.routeProcessDao()
    }

    val allRouteProcesses: Flow<List<RouteProcess>> = routeProcessDao.getAllRouteProcesses()

    suspend fun insert(routeProcess: RouteProcess) {
        withContext(Dispatchers.IO) {
            routeProcessDao.insertRouteProcess(routeProcess)
        }
    }

    suspend fun delete(routeProcess: RouteProcess) {
        withContext(Dispatchers.IO) {
            routeProcessDao.deleteRouteProcess(routeProcess)
        }
    }
}

