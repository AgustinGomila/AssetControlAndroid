package com.example.assetControl.data.webservice.dataCollection

import com.example.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.example.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class DataCollectionContentWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun dataCollectionContentGet(
        dataCollectionId: Long,
    ): Array<DataCollectionContentObject>? {
        val any = getWebservice().s(
            "DataCollectionContent_Get",
            arrayOf(
                WsParam("data_collection_id", dataCollectionId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<DataCollectionContentObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(DataCollectionContentObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun dataCollectionContentGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<DataCollectionContentObject>? {
        val any = getWebservice().s(
            "DataCollectionContent_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<DataCollectionContentObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(DataCollectionContentObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun dataCollectionContentDelete(
        dataCollectionId: Long,
    ): Boolean {
        return getWebservice().s(
            "DataCollectionContent_Delete_All",
            arrayOf(
                WsParam("data_collection_id", dataCollectionId)
            )
        ) as Boolean
    }

    @Throws(Exception::class)
    fun dataCollectionContentCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "DataCollectionContent_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}