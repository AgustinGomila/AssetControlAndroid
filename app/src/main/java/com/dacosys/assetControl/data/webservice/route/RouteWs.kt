package com.dacosys.assetControl.data.webservice.route

import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class RouteWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun routeGetAll(
    ): Array<RouteObject> {
        val any = getWebservice().s(
            "Route_GetAll"
        )
        val dObjAl: ArrayList<RouteObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(RouteObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun routeGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<RouteObject> {
        val any = getWebservice().s(
            "Route_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<RouteObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(RouteObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun routeModify(
        userId: Long,
        route: RouteObject,
    ): Long {
        val waSoapObject = SoapObject(getWebservice().namespace, "route")
        waSoapObject.addProperty("route_id", route.route_id)
        waSoapObject.addProperty("description", route.description)
        waSoapObject.addProperty("active", route.active)

        val result = getWebservice().s(
            "Route_Modify",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun routeAdd(
        userId: Long,
        route: RouteObject,
    ): Long {
        val waSoapObject = SoapObject(getWebservice().namespace, "route")
        waSoapObject.addProperty("route_id", route.route_id)
        waSoapObject.addProperty("description", route.description)
        waSoapObject.addProperty("active", route.active)

        val result = getWebservice().s(
            "Route_Add",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun routeCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "Route_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}