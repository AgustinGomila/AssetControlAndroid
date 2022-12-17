package com.dacosys.assetControl.model.routes.routeProcess.wsObject

import com.dacosys.assetControl.model.routes.routeProcessContent.wsObject.RouteProcessContentObject
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.wsGeneral.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class RouteProcessWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun routeProcessGetAll(
    ): Array<RouteProcessObject>? {
        val any = Statics.getWebservice().s(methodName = "RouteProcess_GetAll")
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<RouteProcessObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(RouteProcessObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun routeProcessGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<RouteProcessObject>? {
        val any = Statics.getWebservice().s(
            methodName = "RouteProcess_GetAll_Limit",
            params = arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<RouteProcessObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(RouteProcessObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun routeProcessGet(
        routeProcessId: Long,
    ): RouteProcessObject {
        val soapObject = Statics.getWebservice().s(
            methodName = "RouteProcess_Get",
            params = arrayOf(WsParam("route_process_id", routeProcessId))
        ) as SoapObject
        return RouteProcessObject().getBySoapObject(soapObject)
    }

    @Throws(Exception::class)
    fun routeProcessDelete(
        routeProcessId: Long,
    ): Boolean {
        return Statics.getWebservice().s(
            methodName = "RouteProcess_Delete",
            params = arrayOf(WsParam("route_process_id", routeProcessId))
        ) as Boolean
    }

    @Throws(Exception::class)
    fun routeProcessModify(
        userId: Long,
        routeProcess: RouteProcessObject,
        routeProcessContent: ArrayList<RouteProcessContentObject>,
    ): Long {
        val arSoapObject = SoapObject(Statics.getWebservice().namespace, "route_process")
        arSoapObject.addProperty("user_id", routeProcess.userId)
        arSoapObject.addProperty("route_id", routeProcess.routeId)
        arSoapObject.addProperty("route_process_date", routeProcess.routeProcessDate)
        arSoapObject.addProperty("completed", routeProcess.completed)
        arSoapObject.addProperty("transfered", routeProcess.transfered)
        arSoapObject.addProperty("transfered_date", routeProcess.transferedDate)
        arSoapObject.addProperty("route_process_id", routeProcess.routeProcessId)
        arSoapObject.addProperty("collector_route_process_id", routeProcess.collectorRouteProcessId)

        var arcArrayObject: ArrayList<SoapObject> = addNullContent()
        if (routeProcessContent.size > 0) {
            routeProcessContent.forEach { t ->
                val arcSoapObject = SoapObject(Statics.getWebservice().namespace, "content")

                arcSoapObject.addProperty("route_process_id", t.routeProcessId)
                arcSoapObject.addProperty("data_collection_rule_id", t.dataCollectionRuleId)
                arcSoapObject.addProperty("level", t.level)
                arcSoapObject.addProperty("position", t.position)
                arcSoapObject.addProperty("route_process_status_id", t.routeProcessStatusId)
                arcSoapObject.addProperty("data_collection_id", t.dataCollectionId)
                arcSoapObject.addProperty("route_process_content_id", t.routeProcessContentId)

                arcArrayObject.add(arcSoapObject)
            }
        } else {
            arcArrayObject = addNullContent()
        }

        val result = Statics.getWebservice().s(
            methodName = "RouteProcess_Modify",
            params = arrayOf(WsParam("user_id", userId)),
            soapObjParams1 = arSoapObject,
            soapObjParams2 = arcArrayObject.toTypedArray()
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun routeProcessAdd(
        routeProcess: RouteProcessObject,
        routeProcessContent: ArrayList<RouteProcessContentObject>,
    ): Long {
        val arSoapObject = SoapObject(Statics.getWebservice().namespace, "route_process")
        arSoapObject.addProperty("user_id", routeProcess.userId)
        arSoapObject.addProperty("route_id", routeProcess.routeId)
        arSoapObject.addProperty("route_process_date", routeProcess.routeProcessDate)
        arSoapObject.addProperty("completed", routeProcess.completed)
        arSoapObject.addProperty("transfered", routeProcess.transfered)
        arSoapObject.addProperty("transfered_date", routeProcess.transferedDate)
        arSoapObject.addProperty("route_process_id", routeProcess.routeProcessId)
        arSoapObject.addProperty("collector_route_process_id", routeProcess.collectorRouteProcessId)

        var arcArrayObject: ArrayList<SoapObject> = addNullContent()
        if (routeProcessContent.size > 0) {
            routeProcessContent.forEach { t ->
                val arcSoapObject = SoapObject(Statics.getWebservice().namespace, "content")

                arcSoapObject.addProperty("route_process_id", t.routeProcessId)
                arcSoapObject.addProperty("data_collection_rule_id", t.dataCollectionRuleId)
                arcSoapObject.addProperty("level", t.level)
                arcSoapObject.addProperty("position", t.position)
                arcSoapObject.addProperty("route_process_status_id", t.routeProcessStatusId)
                arcSoapObject.addProperty("data_collection_id", t.dataCollectionId)
                arcSoapObject.addProperty("route_process_content_id", t.routeProcessContentId)

                arcArrayObject.add(arcSoapObject)
            }
        } else {
            arcArrayObject = addNullContent()
        }

        val result = Statics.getWebservice().s(
            methodName = "RouteProcess_Add",
            soapObjParams1 = arSoapObject,
            soapObjParams2 = arcArrayObject.toTypedArray()
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    private fun addNullContent(): ArrayList<SoapObject> {
        val arcArrayObject: ArrayList<SoapObject> = ArrayList()
        val arcSoapObject = SoapObject(Statics.getWebservice().namespace, "content")

        arcSoapObject.addProperty("route_process_id", null)
        arcSoapObject.addProperty("data_collection_rule_id", -999)
        arcSoapObject.addProperty("level", null)
        arcSoapObject.addProperty("position", null)
        arcSoapObject.addProperty("route_process_status_id", null)
        arcSoapObject.addProperty("data_collection_id", null)
        arcSoapObject.addProperty("route_process_content_id", null)

        arcArrayObject.add(arcSoapObject)

        return arcArrayObject
    }

    @Throws(Exception::class)
    fun routeProcessCount(date: String): Int? {
        val result = Statics.getWebservice().s(
            methodName = "RouteProcess_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}