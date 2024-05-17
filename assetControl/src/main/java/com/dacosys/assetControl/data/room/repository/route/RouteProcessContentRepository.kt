package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.enums.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.dao.route.RouteProcessContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.entity.route.RouteProcessContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class RouteProcessContentRepository {
    private val dao: RouteProcessContentDao
        get() = database.routeProcessContentDao()

    fun select() = dao.select()

    fun selectByRouteProcessId(id: Long) = dao.selectByRouteProcessId(id)

    fun selectByRouteProcessComplete(
        routeId: Long,
        routeProcessId: Long,
        dataCollection: DataCollection?,
    ): List<RouteProcessContent> {
        val rpContArray = selectByRouteProcessId(routeProcessId)
        val rCompArray = RouteCompositionRepository().selectByRouteId(routeId)

        var anyInsert = false

        if (rCompArray.isNotEmpty()) {
            for (rComp in rCompArray) {
                var isIn = false

                if (rpContArray.isNotEmpty()) {
                    for (rpCont in rpContArray) {
                        if (
                            rComp.dataCollectionRuleId == rpCont.dataCollectionRuleId &&
                            rComp.routeId == rpCont.routeId &&
                            rComp.level == rpCont.level &&
                            rComp.position == rpCont.position
                        ) {
                            isIn = true
                            continue
                        }
                    }
                }

                if (!isIn) {
                    anyInsert = true

                    var dataCollectionId: Long? = null
                    if (dataCollection != null) {
                        dataCollectionId = dataCollection.dataCollectionId
                    }

                    val rpc = RouteProcessContent(
                        routeProcessId = routeProcessId,
                        dataCollectionRuleId = rComp.dataCollectionRuleId,
                        level = rComp.level,
                        position = rComp.position,
                        routeProcessStatusId = RouteProcessStatus.notProcessed.id.toLong(),
                        dataCollectionId = dataCollectionId
                    )
                    insert(rpc)
                }
            }
        }

        return if (anyInsert) {
            selectByRouteProcessId(routeProcessId)
        } else rpContArray
    }


    val minId get() = dao.selectMinId() ?: -1

    fun insert(content: RouteProcessContent): Long {
        val r = runBlocking {
            insertSuspend(content)
        }
        return r
    }

    private suspend fun insertSuspend(content: RouteProcessContent): Long {
        val r = withContext(Dispatchers.IO) {
            dao.insert(content)
        }
        return r
    }

    fun update(content: RouteProcessContent): Boolean {
        val r = runBlocking {
            dao.update(content)
            true
        }
        return r
    }

    fun deleteByRouteProcessId(id: Long) = runBlocking {
        dao.deleteByRouteProcessId(id)
    }

    fun deleteByRouteIdRouteProcessDate(minDate: String, rId: Long) = runBlocking {
        dao.deleteByRouteIdRouteProcessDate(minDate, rId)
    }

    fun updateStatusNew(rpc: RouteProcessContent) = runBlocking {
        dao.updateStatusNew(
            id = rpc.id,
            processStatusId = rpc.processStatusId,
            dataCollectionId = rpc.dataCollectionId ?: 0,
            routeProcessId = rpc.routeProcessId,
            dataCollectionRuleId = rpc.dataCollectionRuleId,
            level = rpc.level,
            position = rpc.position,
        )
    }
}
