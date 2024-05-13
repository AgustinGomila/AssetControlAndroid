package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.room.dao.route.RouteDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RouteRepository {
    private val dao: RouteDao by lazy {
        database.routeDao()
    }

    val allRoutes: Flow<List<Route>> = dao.getAllRoutes()

    suspend fun insert(route: Route) {
        withContext(Dispatchers.IO) {
            dao.insertRoute(route)
        }
    }

    suspend fun update(route: Route) {
        withContext(Dispatchers.IO) {
            dao.updateRoute(route)
        }
    }

    suspend fun delete(route: Route) {
        withContext(Dispatchers.IO) {
            dao.deleteRoute(route)
        }
    }
}
