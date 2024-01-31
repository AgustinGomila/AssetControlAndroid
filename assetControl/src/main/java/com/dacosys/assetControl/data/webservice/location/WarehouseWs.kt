package com.dacosys.assetControl.data.webservice.location

import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class WarehouseWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun warehouseGetAll(
    ): Array<WarehouseObject> {
        val any = getWebservice().s(
            "Warehouse_GetAll"
        )
        val dObjAl: ArrayList<WarehouseObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(WarehouseObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun warehouseGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<WarehouseObject> {
        val any = getWebservice().s(
            "Warehouse_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<WarehouseObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(WarehouseObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun warehouseModify(
        userId: Long,
        warehouse: WarehouseObject,
    ): Long {
        val wSoapObject = SoapObject(getWebservice().namespace, "warehouse")
        wSoapObject.addProperty("warehouse_id", warehouse.warehouse_id)
        wSoapObject.addProperty("description", warehouse.description)
        wSoapObject.addProperty("active", warehouse.active)
        wSoapObject.addProperty("warehouse_ext_id", warehouse.warehouse_ext_id)

        val result = getWebservice().s(
            "Warehouse_Modify",
            arrayOf(WsParam("user_id", userId)),
            wSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun warehouseAdd(
        userId: Long,
        warehouse: WarehouseObject,
    ): Long {
        val wSoapObject = SoapObject(getWebservice().namespace, "warehouse")
        wSoapObject.addProperty("warehouse_id", warehouse.warehouse_id)
        wSoapObject.addProperty("description", warehouse.description)
        wSoapObject.addProperty("active", warehouse.active)
        wSoapObject.addProperty("warehouse_ext_id", warehouse.warehouse_ext_id)

        val result = getWebservice().s(
            methodName = "Warehouse_Add",
            params = arrayOf(WsParam("user_id", userId)),
            soapObjParams1 = wSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun warehouseCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "Warehouse_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}