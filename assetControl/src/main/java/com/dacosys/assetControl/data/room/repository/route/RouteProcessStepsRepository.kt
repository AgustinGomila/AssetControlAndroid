package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.room.dao.route.RouteProcessStepsDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.RouteProcessSteps
import kotlinx.coroutines.runBlocking

class RouteProcessStepsRepository {
    private val dao: RouteProcessStepsDao
        get() = database.routeProcessStepsDao()


    fun selectByRouteProcessId(routeProcessId: Long) = dao.selectByRouteProcessId(routeProcessId)


    fun insert(content: RouteProcessSteps) {
        runBlocking {
            dao.insert(content)
        }
    }


    fun deleteByRouteIdRouteProcessDate(minDate: String, rId: Long) = runBlocking {
        dao.deleteByRouteIdRouteProcessDate(minDate, rId)
    }


    fun deleteByRouteProcessId(id: Long) = runBlocking {
        dao.deleteByRouteProcessId(id)
    }

    fun deleteByCollectorDataCollectionId(id: Long) = runBlocking {
        dao.deleteByCollectorDataCollectionId(id)
    }
}