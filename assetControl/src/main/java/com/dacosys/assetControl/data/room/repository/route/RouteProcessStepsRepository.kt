package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.room.dao.route.RouteProcessStepsDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.RouteProcessSteps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RouteProcessStepsRepository {
    private val dao: RouteProcessStepsDao by lazy {
        database.routeProcessStepsDao()
    }

    val allRouteProcessSteps: Flow<List<RouteProcessSteps>> = dao.getAllRouteProcessSteps()

    suspend fun insertRouteProcessSteps(routeProcessSteps: RouteProcessSteps) {
        withContext(Dispatchers.IO) {
            dao.insertRouteProcessSteps(routeProcessSteps)
        }
    }
}