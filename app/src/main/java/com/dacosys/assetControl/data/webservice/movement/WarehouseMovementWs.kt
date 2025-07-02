package com.dacosys.assetControl.data.webservice.movement

import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class WarehouseMovementWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun warehouseMovementGetAll(
    ): Array<WarehouseMovementObject>? {
        val any = getWebservice().s(
            "WarehouseMovement_GetAll"
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<WarehouseMovementObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(WarehouseMovementObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun warehouseMovementGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<WarehouseMovementObject>? {
        val any = getWebservice().s(
            "WarehouseMovement_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<WarehouseMovementObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(WarehouseMovementObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun warehouseMovementGet(
        warehouseMovementId: Long,
    ): WarehouseMovementObject {
        val soapObject = getWebservice().s(
            "WarehouseMovement_Get",
            arrayOf(
                WsParam("warehouse_movement_id", warehouseMovementId)
            )
        ) as SoapObject
        return WarehouseMovementObject().getBySoapObject(soapObject)
    }

    @Throws(Exception::class)
    fun warehouseMovementModify(
        userId: Long,
        warehouseMovement: WarehouseMovementObject,
        warehouseMovementContent: ArrayList<WarehouseMovementContentObject>,
    ): Long {
        val arSoapObject = SoapObject(getWebservice().namespace, "warehouse_movement")
        arSoapObject.addProperty("warehouse_movement_id", warehouseMovement.warehouseMovementId)
        arSoapObject.addProperty("warehouse_movement_date", warehouseMovement.warehouseMovementDate)
        arSoapObject.addProperty("obs", warehouseMovement.obs)
        arSoapObject.addProperty("user_id", warehouseMovement.userId)
        arSoapObject.addProperty("origin_warehouse_area_id", warehouseMovement.origWarehouseAreaId)
        arSoapObject.addProperty("origin_warehouse_id", warehouseMovement.origWarehouseId)
        arSoapObject.addProperty(
            "destination_warehouse_area_id",
            warehouseMovement.destWarehouseAreaId
        )
        arSoapObject.addProperty("destination_warehouse_id", warehouseMovement.destWarehouseId)
        arSoapObject.addProperty("transfered_date", warehouseMovement.transferedDate)
        arSoapObject.addProperty("completed", warehouseMovement.completed)
        arSoapObject.addProperty(
            "collector_warehouse_movement_id",
            warehouseMovement.collectorWarehouseMovementId
        )

        var arcArrayObject: ArrayList<SoapObject> = addNullContent()
        if (warehouseMovementContent.isNotEmpty()) {
            warehouseMovementContent.forEach { t ->
                val arcSoapObject = SoapObject(getWebservice().namespace, "content")

                arcSoapObject.addProperty("warehouse_movement_id", t.warehouseMovementId)
                arcSoapObject.addProperty(
                    "warehouse_movement_content_id",
                    t.warehouseMovementContentId
                )
                arcSoapObject.addProperty("asset_id", t.assetId)
                arcSoapObject.addProperty("code", t.code)
                arcSoapObject.addProperty("qty", t.qty)

                arcArrayObject.add(arcSoapObject)
            }
        } else {
            arcArrayObject = addNullContent()
        }

        val result = getWebservice().s(
            "WarehouseMovement_Modify",
            arrayOf(WsParam("user_id", userId)),
            arSoapObject,
            arcArrayObject.toTypedArray()
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun warehouseMovementAdd(
        warehouseMovement: WarehouseMovementObject,
        warehouseMovementContent: ArrayList<WarehouseMovementContentObject>,
    ): Long {
        val arSoapObject = SoapObject(getWebservice().namespace, "warehouse_movement")
        arSoapObject.addProperty("warehouse_movement_id", warehouseMovement.warehouseMovementId)
        arSoapObject.addProperty("warehouse_movement_date", warehouseMovement.warehouseMovementDate)
        arSoapObject.addProperty("obs", warehouseMovement.obs)
        arSoapObject.addProperty("user_id", warehouseMovement.userId)
        arSoapObject.addProperty("origin_warehouse_area_id", warehouseMovement.origWarehouseAreaId)
        arSoapObject.addProperty("origin_warehouse_id", warehouseMovement.origWarehouseId)
        arSoapObject.addProperty(
            "destination_warehouse_area_id",
            warehouseMovement.destWarehouseAreaId
        )
        arSoapObject.addProperty("destination_warehouse_id", warehouseMovement.destWarehouseId)
        arSoapObject.addProperty("transfered_date", warehouseMovement.transferedDate)
        arSoapObject.addProperty("completed", warehouseMovement.completed)
        arSoapObject.addProperty(
            "collector_warehouse_movement_id",
            warehouseMovement.collectorWarehouseMovementId
        )

        var arcArrayObject: ArrayList<SoapObject> = addNullContent()
        if (warehouseMovementContent.isNotEmpty()) {
            warehouseMovementContent.forEach { t ->
                val arcSoapObject = SoapObject(getWebservice().namespace, "content")

                arcSoapObject.addProperty("warehouse_movement_id", t.warehouseMovementId)
                arcSoapObject.addProperty(
                    "warehouse_movement_content_id",
                    t.warehouseMovementContentId
                )
                arcSoapObject.addProperty("asset_id", t.assetId)
                arcSoapObject.addProperty("code", t.code)
                arcSoapObject.addProperty("qty", t.qty)

                arcArrayObject.add(arcSoapObject)
            }
        } else {
            arcArrayObject = addNullContent()
        }

        val result = getWebservice().s(
            "WarehouseMovement_Add",
            arSoapObject,
            arcArrayObject.toTypedArray()

        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    private fun addNullContent(): ArrayList<SoapObject> {
        val arcArrayObject: ArrayList<SoapObject> = ArrayList()
        val arcSoapObject = SoapObject(getWebservice().namespace, "content")

        arcSoapObject.addProperty("warehouse_movement_id", null)
        arcSoapObject.addProperty("warehouse_movement_content_id", null)
        arcSoapObject.addProperty("asset_id", -999)
        arcSoapObject.addProperty("code", null)
        arcSoapObject.addProperty("qty", null)

        arcArrayObject.add(arcSoapObject)

        return arcArrayObject
    }

    @Throws(Exception::class)
    fun warehouseMovementCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "WarehouseMovement_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}