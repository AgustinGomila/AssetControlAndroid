package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.room.dao.route.RouteCompositionDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.RouteComposition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RouteCompositionRepository {
    private val dao: RouteCompositionDao by lazy {
        database.routeCompositionDao()
    }

    val allRouteCompositions: Flow<List<RouteComposition>> = dao.getAllRouteCompositions()

    suspend fun insert(routeComposition: RouteComposition) {
        withContext(Dispatchers.IO) {
            dao.insertRouteComposition(routeComposition)
        }
    }

    suspend fun update(routeComposition: RouteComposition) {
        withContext(Dispatchers.IO) {
            dao.updateRouteComposition(routeComposition)
        }
    }

    suspend fun delete(routeComposition: RouteComposition) {
        withContext(Dispatchers.IO) {
            dao.deleteRouteComposition(routeComposition)
        }
    }
}