package com.dacosys.assetControl.webservice.dataCollection

import com.dacosys.assetControl.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class DataCollectionRuleTargetWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun dataCollectionRuleTargetGet(
        dataCollectionRuleId: Long,
    ): Array<DataCollectionRuleTargetObject>? {
        val any = getWebservice().s(
            "DataCollectionRuleTarget_Get",
            arrayOf(
                WsParam("data_collection_rule_id", dataCollectionRuleId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<DataCollectionRuleTargetObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(DataCollectionRuleTargetObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    fun dataCollectionRuleTargetDelete(
        dataCollectionRuleId: Long,
    ): Boolean {
        val result = getWebservice().s(
            "DataCollectionRuleTarget_Remove",
            arrayOf(WsParam("data_collection_rule_id", dataCollectionRuleId))
        ) ?: return false
        return result as Boolean
    }

    @Throws(Exception::class)
    fun dataCollectionRuleTargetAdd(
        userId: Long,
        dataCollectionRuleTarget: DataCollectionRuleTargetObject,
    ): Long {
        val icSoapObject =
            SoapObject(getWebservice().namespace, "data_collection_rule_target")
        icSoapObject.addProperty(
            "data_collection_rule_id",
            dataCollectionRuleTarget.dataCollectionRuleId
        )
        icSoapObject.addProperty("asset_id", dataCollectionRuleTarget.assetId)
        icSoapObject.addProperty("warehouse_id", dataCollectionRuleTarget.warehouseId)
        icSoapObject.addProperty("warehouse_area_id", dataCollectionRuleTarget.warehouseAreaId)
        icSoapObject.addProperty("item_category_id", dataCollectionRuleTarget.itemCategoryId)

        val result = getWebservice().s(
            "DataCollectionRuleTarget_Add",
            arrayOf(WsParam("user_id", userId)),
            icSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }
}