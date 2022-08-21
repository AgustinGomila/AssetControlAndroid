package com.dacosys.assetControl.model.routes.routeComposition.wsObject

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.wsGeneral.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class RouteCompositionWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun routeCompositionGet(
        routeId: Long,
    ): Array<RouteCompositionObject>? {
        val any = Statics.getWebservice().s(
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
        val result = Statics.getWebservice().s(
            "RouteComposition_Remove_All",
            arrayOf(WsParam("route_id", routeId))
        ) ?: return 0
        return result as Int
    }
}