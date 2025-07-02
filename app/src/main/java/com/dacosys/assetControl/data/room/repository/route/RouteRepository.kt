package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dao.route.RouteDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.route.Route
import com.dacosys.assetControl.data.room.dto.route.Route.CREATOR.getAvailableRoutes
import com.dacosys.assetControl.data.webservice.route.RouteObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class RouteRepository {
    private val dao: RouteDao
        get() = database.routeDao()

    fun select(onlyActive: Boolean) = runBlocking {
        getAvailableRoutes(
            if (onlyActive) dao.selectActive()
            else dao.select()
        )
    }

    fun selectByDescription(desc: String, onlyActive: Boolean) = runBlocking {
        getAvailableRoutes(
            if (onlyActive) dao.selectByDescriptionOnlyActive(desc)
            else dao.selectByDescription(desc)
        )
    }


    suspend fun sync(
        routeObjects: Array<RouteObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.Route

        val routes: ArrayList<Route> = arrayListOf()
        routeObjects.mapTo(routes) { Route(it) }
        val partial = routes.count()

        withContext(Dispatchers.IO) {
            dao.insert(routes) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = context.getString(R.string.synchronizing_routes),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }
}
