package com.dacosys.assetControl.data.webservice.dataCollection

import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class DataCollectionRuleContentWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun dataCollectionRuleContentGet(
        dataCollectionRuleId: Long,
    ): Array<DataCollectionRuleContentObject>? {
        val any = getWebservice().s(
            "DataCollectionRuleContent_Get",
            arrayOf(
                WsParam("data_collection_rule_id", dataCollectionRuleId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<DataCollectionRuleContentObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(DataCollectionRuleContentObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun dataCollectionRuleContentModify(
        userId: Long,
        dataCollectionRuleContent: DataCollectionRuleContentObject,
    ): Long {
        val dcrcSoapObject =
            SoapObject(getWebservice().namespace, "data_collection_rule_content")

        dcrcSoapObject.addProperty(
            "data_collection_rule_id",
            dataCollectionRuleContent.dataCollectionRuleId
        )
        dcrcSoapObject.addProperty(
            "data_collection_rule_content_id",
            dataCollectionRuleContent.dataCollectionRuleContentId
        )
        dcrcSoapObject.addProperty("level", dataCollectionRuleContent.level)
        dcrcSoapObject.addProperty("position", dataCollectionRuleContent.position)
        dcrcSoapObject.addProperty("description", dataCollectionRuleContent.description)
        dcrcSoapObject.addProperty("attribute_id", dataCollectionRuleContent.attributeId)
        dcrcSoapObject.addProperty(
            "attribute_composition_id",
            dataCollectionRuleContent.attributeCompositionId
        )
        dcrcSoapObject.addProperty("expression", dataCollectionRuleContent.expression)
        dcrcSoapObject.addProperty("true_result", dataCollectionRuleContent.trueResult)
        dcrcSoapObject.addProperty("false_result", dataCollectionRuleContent.falseResult)
        dcrcSoapObject.addProperty("mandatory", dataCollectionRuleContent.mandatory)
        dcrcSoapObject.addProperty("active", dataCollectionRuleContent.active)

        val result = getWebservice().s(
            "DataCollectionRuleContent_Modify",
            arrayOf(WsParam("user_id", userId)),
            dcrcSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun dataCollectionRuleContentAdd(
        dataCollectionRuleContent: DataCollectionRuleContentObject,
    ): Long {
        val dcrcSoapObject =
            SoapObject(getWebservice().namespace, "data_collection_rule_content")

        dcrcSoapObject.addProperty(
            "data_collection_rule_id",
            dataCollectionRuleContent.dataCollectionRuleId
        )
        dcrcSoapObject.addProperty(
            "data_collection_rule_content_id",
            dataCollectionRuleContent.dataCollectionRuleContentId
        )
        dcrcSoapObject.addProperty("level", dataCollectionRuleContent.level)
        dcrcSoapObject.addProperty("position", dataCollectionRuleContent.position)
        dcrcSoapObject.addProperty("description", dataCollectionRuleContent.description)
        dcrcSoapObject.addProperty("attribute_id", dataCollectionRuleContent.attributeId)
        dcrcSoapObject.addProperty(
            "attribute_composition_id",
            dataCollectionRuleContent.attributeCompositionId
        )
        dcrcSoapObject.addProperty("expression", dataCollectionRuleContent.expression)
        dcrcSoapObject.addProperty("true_result", dataCollectionRuleContent.trueResult)
        dcrcSoapObject.addProperty("false_result", dataCollectionRuleContent.falseResult)
        dcrcSoapObject.addProperty("mandatory", dataCollectionRuleContent.mandatory)
        dcrcSoapObject.addProperty("active", dataCollectionRuleContent.active)

        val result = getWebservice().s(
            "DataCollectionRuleContent_Add",
            dcrcSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }
}