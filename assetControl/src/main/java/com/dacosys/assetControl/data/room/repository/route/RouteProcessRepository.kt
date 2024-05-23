package com.dacosys.assetControl.data.room.repository.route

import com.dacosys.assetControl.data.room.dao.route.RouteProcessDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.route.Route
import com.dacosys.assetControl.data.room.dto.route.RouteProcess
import com.dacosys.assetControl.data.room.entity.route.RouteProcessEntity
import com.dacosys.assetControl.utils.misc.UTCDataTime.Companion.dateToNotNullStringDate
import com.dacosys.assetControl.utils.misc.UTCDataTime.Companion.getUTCDateTimeAsNotNullDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RouteProcessRepository {
    private val dao: RouteProcessDao
        get() = database.routeProcessDao()

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectByRouteIdNoCompleted(routeId: Long) = runBlocking { dao.selectByRouteIdNoCompleted(routeId) }

    fun selectByNoTransferred() = runBlocking { dao.selectByNoTransferred() }

    private fun selectTransferred() = runBlocking { dao.selectTransferred() }

    fun selectByNoCompleted() = runBlocking { dao.selectByNoCompleted() }

    private val nextLastId: Long
        get() = runBlocking {
            val minId = dao.selectMinId() ?: 0
            if (minId > 0) -1 else minId - 1
        }


    fun insert(userId: Long, route: Route): RouteProcess? {
        val nextId = nextLastId

        val r = runBlocking {
            val rp = RouteProcess(
                id = nextId,
                userId = userId,
                routeId = route.id,
                routeProcessDate = getUTCDateTimeAsNotNullDate(),
                routeStr = route.description
            )
            dao.insert(RouteProcessEntity(rp))
            dao.selectById(nextId)
        }
        return r
    }


    fun update(content: RouteProcess): Boolean {
        val r = runBlocking {
            updateSuspend(content)
        }
        return r
    }

    private suspend fun updateSuspend(content: RouteProcess): Boolean {
        withContext(Dispatchers.IO) {
            dao.update(RouteProcessEntity(content))
        }
        return true
    }

    fun updateTransferred(newValue: Long, oldValue: Long) {
        runBlocking {
            val date = getUTCDateTimeAsNotNullDate()
            dao.updateId(newValue, oldValue, date)
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

                        minDate = dateToNotNullStringDate(r.routeProcessDate)
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

