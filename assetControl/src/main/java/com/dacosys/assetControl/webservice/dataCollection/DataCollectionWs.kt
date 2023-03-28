package com.dacosys.assetControl.webservice.dataCollection

import com.dacosys.assetControl.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class DataCollectionWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun dataCollectionGetAll(
    ): Array<DataCollectionObject>? {
        val any = getWebservice().s(
            "DataCollection_GetAll"
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<DataCollectionObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(DataCollectionObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun dataCollectionGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<DataCollectionObject>? {
        val any = getWebservice().s(
            "DataCollection_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<DataCollectionObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(DataCollectionObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun dataCollectionGet(
        dataCollectionId: Long,
    ): DataCollectionObject {
        val soapObject = getWebservice().s(
            "DataCollection_Get",
            arrayOf(
                WsParam("data_collection_id", dataCollectionId)
            )
        ) as SoapObject
        return DataCollectionObject().getBySoapObject(soapObject)
    }

    @Throws(Exception::class)
    fun dataCollectionModify(
        userId: Long,
        dataCollection: DataCollectionObject,
        dataCollectionContent: ArrayList<DataCollectionContentObject>,
    ): Long {
        val arSoapObject = SoapObject(getWebservice().namespace, "data_collection")
        arSoapObject.addProperty("data_collection_id", dataCollection.dataCollectionId)
        arSoapObject.addProperty("asset_id", dataCollection.assetId)
        arSoapObject.addProperty("warehouse_id", dataCollection.warehouseId)
        arSoapObject.addProperty("warehouse_area_id", dataCollection.warehouseAreaId)
        arSoapObject.addProperty("user_id", dataCollection.userId)
        arSoapObject.addProperty("date_start", dataCollection.dateStart)
        arSoapObject.addProperty("date_end", dataCollection.dateEnd)
        arSoapObject.addProperty("completed", dataCollection.completed)
        arSoapObject.addProperty("transfered_date", dataCollection.transferedDate)
        arSoapObject.addProperty(
            "collector_data_collection_id",
            dataCollection.collectorDataCollectionId
        )
        arSoapObject.addProperty(
            "collector_route_process_id",
            dataCollection.collectorRouteProcessId
        )

        var arcArrayObject: ArrayList<SoapObject> = addNullContent()
        if (dataCollectionContent.size > 0) {
            dataCollectionContent.forEach { t ->
                val arcSoapObject = SoapObject(getWebservice().namespace, "content")

                arcSoapObject.addProperty("data_collection_id", t.dataCollectionId)
                arcSoapObject.addProperty("level", t.level)
                arcSoapObject.addProperty("position", t.position)
                arcSoapObject.addProperty("attribute_id", t.attributeId)
                arcSoapObject.addProperty("attribute_composition_id", t.attributeCompositionId)
                arcSoapObject.addProperty("result", t.result)
                arcSoapObject.addProperty("value_str", t.valueStr)
                arcSoapObject.addProperty("value", t.valueStr)
                arcSoapObject.addProperty("data_collection_date", t.dataCollectionDate)
                arcSoapObject.addProperty("data_collection_content_id", t.dataCollectionContentId)
                arcSoapObject.addProperty(
                    "collector_data_collection_content_id",
                    t.collectorDataCollectionContentId
                )
                arcSoapObject.addProperty(
                    "data_collection_rule_content_id",
                    t.dataCollectionRuleContentId
                )

                arcArrayObject.add(arcSoapObject)
            }
        } else {
            arcArrayObject = addNullContent()
        }

        val result = getWebservice().s(
            "DataCollection_Modify",
            arrayOf(WsParam("user_id", userId)),
            arSoapObject,
            arcArrayObject.toTypedArray()
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun dataCollectionAdd(
        dataCollection: DataCollectionObject,
        dataCollectionContent: ArrayList<DataCollectionContentObject>,
    ): Long {
        val arSoapObject = SoapObject(getWebservice().namespace, "data_collection")
        arSoapObject.addProperty("data_collection_id", dataCollection.dataCollectionId)
        arSoapObject.addProperty("asset_id", dataCollection.assetId)
        arSoapObject.addProperty("warehouse_id", dataCollection.warehouseId)
        arSoapObject.addProperty("warehouse_area_id", dataCollection.warehouseAreaId)
        arSoapObject.addProperty("user_id", dataCollection.userId)
        arSoapObject.addProperty("date_start", dataCollection.dateStart)
        arSoapObject.addProperty("date_end", dataCollection.dateEnd)
        arSoapObject.addProperty("completed", dataCollection.completed)
        arSoapObject.addProperty("transfered_date", dataCollection.transferedDate)
        arSoapObject.addProperty(
            "collector_data_collection_id",
            dataCollection.collectorDataCollectionId
        )
        arSoapObject.addProperty(
            "collector_route_process_id",
            dataCollection.collectorRouteProcessId
        )

        var arcArrayObject: ArrayList<SoapObject> = addNullContent()
        if (dataCollectionContent.size > 0) {
            dataCollectionContent.forEach { t ->
                val arcSoapObject = SoapObject(getWebservice().namespace, "content")

                arcSoapObject.addProperty("data_collection_id", t.dataCollectionId)
                arcSoapObject.addProperty("level", t.level)
                arcSoapObject.addProperty("position", t.position)
                arcSoapObject.addProperty("attribute_id", t.attributeId)
                arcSoapObject.addProperty("attribute_composition_id", t.attributeCompositionId)
                arcSoapObject.addProperty("result", t.result)
                arcSoapObject.addProperty("value_str", t.valueStr)
                arcSoapObject.addProperty("value", t.valueStr)
                arcSoapObject.addProperty("data_collection_date", t.dataCollectionDate)
                arcSoapObject.addProperty("data_collection_content_id", t.dataCollectionContentId)
                arcSoapObject.addProperty(
                    "collector_data_collection_content_id",
                    t.collectorDataCollectionContentId
                )
                arcSoapObject.addProperty(
                    "data_collection_rule_content_id",
                    t.dataCollectionRuleContentId
                )

                arcArrayObject.add(arcSoapObject)
            }
        } else {
            arcArrayObject = addNullContent()
        }

        val result = getWebservice().s(
            "DataCollection_Add",
            arSoapObject,
            arcArrayObject.toTypedArray()

        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    private fun addNullContent(): ArrayList<SoapObject> {
        val arcArrayObject: ArrayList<SoapObject> = ArrayList()
        val arcSoapObject = SoapObject(getWebservice().namespace, "content")

        arcSoapObject.addProperty("data_collection_id", null)
        arcSoapObject.addProperty("level", null)
        arcSoapObject.addProperty("position", null)
        arcSoapObject.addProperty("attribute_id", -999)
        arcSoapObject.addProperty("attribute_composition_id", null)
        arcSoapObject.addProperty("result", null)
        arcSoapObject.addProperty("value_str", null)
        arcSoapObject.addProperty("value", null)
        arcSoapObject.addProperty("data_collection_date", null)
        arcSoapObject.addProperty("data_collection_content_id", null)
        arcSoapObject.addProperty("collector_data_collection_content_id", null)
        arcSoapObject.addProperty("data_collection_rule_content_id", null)

        arcArrayObject.add(arcSoapObject)

        return arcArrayObject
    }

    @Throws(Exception::class)
    fun dataCollectionCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "DataCollection_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}