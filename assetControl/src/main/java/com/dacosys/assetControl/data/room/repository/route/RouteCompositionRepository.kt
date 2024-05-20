package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.room.dao.route.RouteCompositionDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.RouteComposition
import kotlinx.coroutines.runBlocking

class RouteCompositionRepository {
    private val dao: RouteCompositionDao
        get() = database.routeCompositionDao()

    fun selectByRouteId(routeId: Long) = runBlocking { dao.selectByRouteId(routeId) }


    fun insert(compositions: List<RouteComposition>) = runBlocking {
        dao.insert(compositions)
    }


    fun deleteByRouteId(routeId: Long) = runBlocking {
        dao.deleteByRouteId(routeId)
    }
}