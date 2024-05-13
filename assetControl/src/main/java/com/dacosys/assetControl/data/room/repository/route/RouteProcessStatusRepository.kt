package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.room.dao.route.RouteProcessStatusDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RouteProcessStatusRepository {
    private val dao: RouteProcessStatusDao by lazy {
        database.routeProcessStatusDao()
    }

    val allRouteProcessStatus: Flow<List<RouteProcessStatus>> = dao.getAllRouteProcessStatus()

    suspend fun insert(routeProcessStatus: RouteProcessStatus) {
        withContext(Dispatchers.IO) {
            dao.insertRouteProcessStatus(routeProcessStatus)
        }
    }

    suspend fun delete(routeProcessStatus: RouteProcessStatus) {
        withContext(Dispatchers.IO) {
            dao.deleteRouteProcessStatus(routeProcessStatus)
        }
    }
}