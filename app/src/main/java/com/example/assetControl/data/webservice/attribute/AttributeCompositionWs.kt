package com.example.assetControl.data.webservice.attribute

import com.example.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.example.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class AttributeCompositionWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun attributeCompositionGet(
        attributeId: Long,
    ): Array<AttributeCompositionObject>? {
        val any = getWebservice().s(
            "AttributeComposition_Get",
            arrayOf(
                WsParam("attribute_id", attributeId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<AttributeCompositionObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(AttributeCompositionObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun attributeCompositionDeleteAll(
        attributeId: Int,
    ): Int {
        val result = getWebservice().s(
            "AttributeComposition_Remove_All",
            arrayOf(WsParam("attribute_id", attributeId))
        ) ?: return 0
        return result as Int
    }

    @Throws(Exception::class)
    fun attributeCompositionUpdateIsUsed(): Boolean {
        val result = getWebservice().s(
            "AttributeComposition_Update_IsUsed"
        ) ?: return false
        return result as Boolean
    }
}