package com.example.assetControl.data.webservice.movement

import com.example.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.example.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class WarehouseMovementContentWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun warehouseMovementContentGet(
        warehouseMovementId: Long,
    ): Array<WarehouseMovementContentObject>? {
        val any = getWebservice().s(
            "WarehouseMovementContent_Get",
            arrayOf(
                WsParam("warehouse_movement_id", warehouseMovementId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<WarehouseMovementContentObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(WarehouseMovementContentObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun warehouseMovementContentGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<WarehouseMovementContentObject>? {
        val any = getWebservice().s(
            "WarehouseMovementContent_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<WarehouseMovementContentObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(WarehouseMovementContentObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun warehouseMovementContentDelete(
        warehouseMovementId: Long,
    ): Boolean {
        return getWebservice().s(
            "WarehouseMovementContent_Remove_All",
            arrayOf(
                WsParam("warehouse_movement_id", warehouseMovementId)
            )
        ) as Boolean
    }

    @Throws(Exception::class)
    fun warehouseMovementContentCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "WarehouseMovementContent_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}