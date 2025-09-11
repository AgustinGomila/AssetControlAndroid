package com.example.assetControl.data.webservice.route

import com.example.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.example.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class RouteCompositionWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun routeCompositionGet(
        routeId: Long,
    ): Array<RouteCompositionObject>? {
        val any = getWebservice().s(
            "RouteComposition_Get",
            arrayOf(
                WsParam("route_id", routeId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<RouteCompositionObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(RouteCompositionObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun routeCompositionDeleteAll(
        routeId: Int,
    ): Int {
        val result = getWebservice().s(
            "RouteComposition_Remove_All",
            arrayOf(WsParam("route_id", routeId))
        ) ?: return 0
        return result as Int
    }
}