package com.dacosys.assetControl.webservice.route

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class RouteProcessContentWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun routeProcessContentGet(
        routeProcessId: Long,
    ): Array<RouteProcessContentObject>? {
        val any = Statics.getWebservice().s(
            "RouteProcessContent_Get",
            arrayOf(

                WsParam("route_process_id", routeProcessId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<RouteProcessContentObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(RouteProcessContentObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun routeProcessContentGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<RouteProcessContentObject>? {
        val any = Statics.getWebservice().s(
            "RouteProcessContent_GetAll_Limit",
            arrayOf(

                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<RouteProcessContentObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(RouteProcessContentObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun routeProcessContentDelete(
        routeProcessId: Long,
    ): Boolean {
        return Statics.getWebservice().s(
            "RouteProcessContent_Delete_All",
            arrayOf(

                WsParam("route_process_id", routeProcessId)
            )
        ) as Boolean
    }

    @Throws(Exception::class)
    fun routeProcessContentCount(date: String): Int? {
        val result = Statics.getWebservice().s(
            methodName = "RouteProcessContent_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}