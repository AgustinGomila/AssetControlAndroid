package com.example.assetControl.data.room.repository.route

import com.example.assetControl.data.enums.route.RouteProcessStatus
import com.example.assetControl.data.room.dao.route.RouteProcessContentDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.dataCollection.DataCollection
import com.example.assetControl.data.room.dto.route.RouteProcessContent
import com.example.assetControl.data.room.entity.route.RouteProcessContentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class RouteProcessContentRepository {
    private val dao: RouteProcessContentDao
        get() = database.routeProcessContentDao()

    fun select() = runBlocking { dao.select() }

    fun selectByRouteProcessId(id: Long) = runBlocking { dao.selectByRouteProcessId(id) }

    fun selectByRouteProcessComplete(
        routeId: Long,
        routeProcessId: Long,
        dataCollection: DataCollection?,
    ): List<RouteProcessContent> {
        val rpContArray = selectByRouteProcessId(routeProcessId)
        val rCompArray = RouteCompositionRepository().selectByRouteId(routeId)
        val rpcList: MutableList<RouteProcessContent> = mutableListOf()

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
                    rpcList.add(
                        RouteProcessContent(
                            routeProcessId = routeProcessId,
                            dataCollectionRuleId = rComp.dataCollectionRuleId,
                            level = rComp.level,
                            position = rComp.position,
                            routeProcessStatusId = RouteProcessStatus.notProcessed.id,
                            dataCollectionId = dataCollection?.id
                        )
                    )
                }
            }
        }

        if (rpcList.any()) {
            val ids = insert(rpcList)
            println(ids)
            return selectByRouteProcessId(routeProcessId)
        } else
            return rpContArray
    }

    private val nextLastId: Long
        get() = runBlocking {
            val minId = dao.selectMinId() ?: 0
            if (minId > 0) -1 else minId - 1
        }


    fun insert(content: List<RouteProcessContent>): List<Long> {
        val r: MutableList<Long> = mutableListOf()
        runBlocking {
            var nextId = nextLastId

            content.forEach { c ->
                c.id = nextId
                nextId--
            }
            insertSuspend(content)

            r.add(nextId)
        }
        return r.toList()
    }

    fun insert(content: RouteProcessContent): Long =
        runBlocking {
            val nextId = nextLastId

            content.id = nextId
            insertSuspend(content)

            nextId
        }

    private suspend fun insertSuspend(content: List<RouteProcessContent>): List<Long> {
        val r: MutableList<Long> = mutableListOf()
        withContext(Dispatchers.IO) {
            dao.insert(content.map { RouteProcessContentEntity(it) })
        }
        return r
    }

    private suspend fun insertSuspend(content: RouteProcessContent): Long =
        withContext(Dispatchers.IO) {
            dao.insert(RouteProcessContentEntity(content))
        }


    fun update(content: RouteProcessContent): Boolean {
        val r = runBlocking {
            dao.update(RouteProcessContentEntity(content))
            true
        }
        return r
    }

    fun updateRouteProcessId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateRouteProcessId(newValue, oldValue)
        }
    }

    fun updateDataCollectionId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateDataCollectionId(newValue, oldValue)
        }
    }


    fun deleteByRouteProcessId(id: Long) = runBlocking {
        dao.deleteByRouteProcessId(id)
    }

    fun deleteByRouteIdRouteProcessDate(minDate: String, rId: Long) = runBlocking {
        dao.deleteByRouteIdRouteProcessDate(minDate, rId)
    }

    fun updateStatus(rpc: RouteProcessContent) = runBlocking {
        dao.updateStatus(
            id = rpc.id,
            processStatusId = rpc.routeProcessStatusId,
            dataCollectionId = rpc.dataCollectionId,
            routeProcessId = rpc.routeProcessId,
            dataCollectionRuleId = rpc.dataCollectionRuleId,
            level = rpc.level,
            position = rpc.position,
        )
    }
}
