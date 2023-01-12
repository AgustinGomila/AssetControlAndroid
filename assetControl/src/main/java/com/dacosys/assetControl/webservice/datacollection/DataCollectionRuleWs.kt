package com.dacosys.assetControl.webservice.datacollection

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class DataCollectionRuleWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun dataCollectionRuleGetAll(
    ): Array<DataCollectionRuleObject> {
        val any = Statics.getWebservice().s(
            "DataCollectionRule_GetAll"
        )
        val dObjAl: ArrayList<DataCollectionRuleObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(DataCollectionRuleObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun dataCollectionRuleGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<DataCollectionRuleObject> {
        val any = Statics.getWebservice().s(
            "DataCollectionRule_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<DataCollectionRuleObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(DataCollectionRuleObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun dataCollectionRuleModify(
        userId: Long,
        dataCollectionRule: DataCollectionRuleObject,
    ): Long {
        val waSoapObject = SoapObject(Statics.getWebservice().namespace, "data_collection_rule")
        waSoapObject.addProperty("data_collection_rule_id", dataCollectionRule.dataCollectionRuleId)
        waSoapObject.addProperty("description", dataCollectionRule.description)
        waSoapObject.addProperty("active", dataCollectionRule.active)

        val result = Statics.getWebservice().s(
            "DataCollectionRule_Modify",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun dataCollectionRuleAdd(
        userId: Long,
        dataCollectionRule: DataCollectionRuleObject,
    ): Long {
        val waSoapObject = SoapObject(Statics.getWebservice().namespace, "data_collection_rule")
        waSoapObject.addProperty("data_collection_rule_id", dataCollectionRule.dataCollectionRuleId)
        waSoapObject.addProperty("description", dataCollectionRule.description)
        waSoapObject.addProperty("active", dataCollectionRule.active)

        val result = Statics.getWebservice().s(
            "DataCollectionRule_Add",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun dataCollectionRuleCount(date: String): Int? {
        val result = Statics.getWebservice().s(
            methodName = "DataCollectionRule_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}