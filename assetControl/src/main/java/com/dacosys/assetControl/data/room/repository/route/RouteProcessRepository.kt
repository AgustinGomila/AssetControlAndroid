package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.data.room.dao.route.RouteProcessDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.route.Route
import com.dacosys.assetControl.data.room.entity.route.RouteProcess
import com.dacosys.assetControl.utils.misc.UTCDataTime.Companion.getUTCDateTimeAsDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RouteProcessRepository {
    private val dao: RouteProcessDao
        get() = database.routeProcessDao()

    fun selectById(id: Long) = dao.selectById(id)

    fun selectByRouteIdNoCompleted(routeId: Long) = dao.selectByRouteIdNoCompleted(routeId)

    fun selectByNoTransferred() = dao.selectByNoTransferred()

    private fun selectTransferred() = dao.selectTransferred()

    fun selectByNoCompleted() = dao.selectByNoCompleted()

    private val nextId: Long
        get() = (dao.selectMaxId() ?: 0) + 1


    fun insert(route: Route): Long {
        val nextId = nextId
        val userId = getUserId() ?: return 0

        runBlocking {
            val rp = RouteProcess(
                id = nextId,
                userId = userId,
                routeId = route.id,
                routeProcessDate = getUTCDateTimeAsDate() ?: Date(),
                mCompleted = 0,
                transferred = 0,
                transferredDate = null,
                routeStr = route.description
            )
            dao.insert(rp)
        }

        return nextId
    }


    fun update(content: RouteProcess): Boolean {
        val r = runBlocking {
            updateSuspend(content)
        }
        return r
    }

    private suspend fun updateSuspend(content: RouteProcess): Boolean {
        withContext(Dispatchers.IO) {
            dao.update(content)
        }
        return true
    }

    fun updateTransferredNew(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateId(newValue, oldValue)
        }
    }


    fun deleteById(id: Long) = runBlocking {
        dao.deleteById(id)
    }

    fun deleteTransferred(): Boolean {
        val rp: List<RouteProcess> = selectTransferred()
        if (rp.isEmpty()) {
            return true
        }

        // Necesito los ID de las rutas que tienen recolecciones
        val routeIdList: ArrayList<Long> = ArrayList()
        for (r in ArrayList(rp.sortedWith(compareBy { it.routeProcessDate }).reversed())) {
            if (routeIdList.contains(r.routeId)) {
                continue
            }
            routeIdList.add(r.routeId)
        }

        // Esta es una forma de obtener la fecha actual y restarle 7 días
        val c = Calendar.getInstance()
        c.add(Calendar.DATE, -7)

        val sdf = SimpleDateFormat("dd'/'MM'/'yyyy HH:mm:ss a", Locale.getDefault())
        sdf.calendar = c
        var minDate = sdf.format(c.time).toString()

        val contentRepository = RouteProcessContentRepository()
        val stepsRepository = RouteProcessStepsRepository()

        val r = try {
            for (rId in routeIdList) {
                var a = 0
                for (r in ArrayList(rp.sortedWith(compareBy { it.routeProcessDate }).reversed())) {
                    if (r.routeId == rId) {
                        a++
                        if (a <= 4) {
                            continue
                        }

                        minDate = r.routeProcessDate.toString()
                        break
                    }
                }

                if (a > 4) {
                    contentRepository.deleteByRouteIdRouteProcessDate(minDate, rId)
                    stepsRepository.deleteByRouteIdRouteProcessDate(minDate, rId)
                    deleteByRouteIdRouteProcessDate(minDate, rId)
                }
            }
            true
        } catch (ex: Exception) {
            false
        }

        return r
    }

    private fun deleteByRouteIdRouteProcessDate(minDate: String, rId: Long) = runBlocking {
        dao.deleteByRouteIdRouteProcessDate(minDate, rId)
    }
}

