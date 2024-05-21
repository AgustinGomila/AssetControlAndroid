package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.enums.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.dao.route.RouteProcessContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.dto.route.RouteProcessContent
import com.dacosys.assetControl.data.room.entity.route.RouteProcessContentEntity
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
                    var dataCollectionId: Long? = null
                    if (dataCollection != null) {
                        dataCollectionId = dataCollection.id
                    }

                    rpcList.add(
                        RouteProcessContent(
                            routeProcessId = routeProcessId,
                            dataCollectionRuleId = rComp.dataCollectionRuleId,
                            level = rComp.level,
                            position = rComp.position,
                            routeProcessStatusId = RouteProcessStatus.notProcessed.id,
                            dataCollectionId = dataCollectionId
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

    val minId get() = runBlocking { dao.selectMinId() ?: -1 }


    fun insert(content: List<RouteProcessContent>): List<Long> {
        val r = runBlocking {
            insertSuspend(content)
        }
        return r
    }

    /**
     * Insert
     *
     * @param content
     * @return New ID
     */
    fun insert(content: RouteProcessContent): Long =
        runBlocking {
            insertSuspend(content)
        }

    /**
     * Insert suspend
     *
     * @param content
     * @return List of new IDs
     */
    private suspend fun insertSuspend(content: List<RouteProcessContent>): List<Long> {
        val r: MutableList<Long> = mutableListOf()
        withContext(Dispatchers.IO) {
            dao.insert(content.map { RouteProcessContentEntity(it) })
        }
        return r
    }

    /**
     * Insert suspend
     *
     * @param content
     * @return New ID
     */
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

    fun deleteByRouteProcessId(id: Long) = runBlocking {
        dao.deleteByRouteProcessId(id)
    }

    fun deleteByRouteIdRouteProcessDate(minDate: String, rId: Long) = runBlocking {
        dao.deleteByRouteIdRouteProcessDate(minDate, rId)
    }

    fun updateStatusNew(rpc: RouteProcessContent) = runBlocking {
        dao.updateStatusNew(
            id = rpc.id,
            processStatusId = rpc.routeProcessStatusId,
            dataCollectionId = rpc.dataCollectionId ?: 0,
            routeProcessId = rpc.routeProcessId,
            dataCollectionRuleId = rpc.dataCollectionRuleId,
            level = rpc.level,
            position = rpc.position,
        )
    }
}
