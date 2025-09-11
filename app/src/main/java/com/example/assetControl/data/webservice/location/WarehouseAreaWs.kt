package com.example.assetControl.data.webservice.location

import com.example.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.example.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class WarehouseAreaWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun warehouseAreaGetAll(
    ): Array<WarehouseAreaObject> {
        val any = getWebservice().s(
            "WarehouseArea_GetAll"
        )
        val dObjAl: ArrayList<WarehouseAreaObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(WarehouseAreaObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun warehouseAreaGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<WarehouseAreaObject> {
        val any = getWebservice().s(
            "WarehouseArea_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<WarehouseAreaObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(WarehouseAreaObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun warehouseAreaModify(
        userId: Long,
        warehouseArea: WarehouseAreaObject,
    ): Long {
        val waSoapObject = SoapObject(getWebservice().namespace, "warehouse_area")
        waSoapObject.addProperty("warehouse_area_id", warehouseArea.warehouse_area_id)
        waSoapObject.addProperty("warehouse_id", warehouseArea.warehouse_id)
        waSoapObject.addProperty("description", warehouseArea.description)
        waSoapObject.addProperty("active", warehouseArea.active)
        waSoapObject.addProperty("warehouse_area_ext_id", warehouseArea.warehouse_area_ext_id)

        val result = getWebservice().s(
            "WarehouseArea_Modify",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun warehouseAreaAdd(
        userId: Long,
        warehouseArea: WarehouseAreaObject,
    ): Long {
        val waSoapObject = SoapObject(getWebservice().namespace, "warehouse_area")
        waSoapObject.addProperty("warehouse_area_id", warehouseArea.warehouse_area_id)
        waSoapObject.addProperty("warehouse_id", warehouseArea.warehouse_id)
        waSoapObject.addProperty("description", warehouseArea.description)
        waSoapObject.addProperty("active", warehouseArea.active)
        waSoapObject.addProperty("warehouse_area_ext_id", warehouseArea.warehouse_area_ext_id)

        val result = getWebservice().s(
            "WarehouseArea_Add",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun warehouseAreaCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "WarehouseArea_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}