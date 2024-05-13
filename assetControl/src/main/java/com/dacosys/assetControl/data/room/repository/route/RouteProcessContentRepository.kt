package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.room.dao.route.RouteProcessContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.RouteProcessContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RouteProcessContentRepository {
    private val dao: RouteProcessContentDao by lazy {
        database.routeProcessContentDao()
    }

    suspend fun insertRouteProcessContent(routeProcessContent: RouteProcessContent) {
        withContext(Dispatchers.IO) {
            dao.insertRouteProcessContent(routeProcessContent)
        }
    }

    suspend fun updateRouteProcessContent(routeProcessContent: RouteProcessContent) {
        withContext(Dispatchers.IO) {
            dao.updateRouteProcessContent(routeProcessContent)
        }
    }

    suspend fun deleteRouteProcessContent(routeProcessContent: RouteProcessContent) {
        withContext(Dispatchers.IO) {
            dao.deleteRouteProcessContent(routeProcessContent)
        }
    }

    fun getAllRouteProcessContent(): Flow<List<RouteProcessContent>> {
        return dao.getAllRouteProcessContent()
    }

    // Aquí podrías definir más métodos según tus necesidades
}
