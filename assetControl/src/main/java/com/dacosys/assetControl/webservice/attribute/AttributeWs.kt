package com.dacosys.assetControl.webservice.attribute

import com.dacosys.assetControl.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class AttributeWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun attributeGetAll(
    ): Array<AttributeObject> {
        val any = getWebservice().s(methodName = "Attribute_GetAll")
        val dObjAl: ArrayList<AttributeObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(AttributeObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun attributeGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<AttributeObject> {
        val any = getWebservice().s(
            methodName = "Attribute_GetAll_Limit",
            params = arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<AttributeObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(AttributeObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun attributeModify(
        userId: Long,
        attribute: AttributeObject,
    ): Long {
        val aSoapObject = SoapObject(getWebservice().namespace, "attribute")
        aSoapObject.addProperty("attribute_id", attribute.attributeId)
        aSoapObject.addProperty("attribute_type_id", attribute.attributeTypeId)
        aSoapObject.addProperty("description", attribute.description)
        aSoapObject.addProperty("active", attribute.active)
        aSoapObject.addProperty("attribute_category_id", attribute.attributeCategoryId)

        val result = getWebservice().s(
            methodName = "Attribute_Modify",
            params = arrayOf(WsParam("user_id", userId)),
            soapObjParams1 = aSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun attributeAdd(
        userId: Long,
        attribute: AttributeObject,
    ): Long {
        val aSoapObject = SoapObject(getWebservice().namespace, "attribute")
        aSoapObject.addProperty("attribute_id", attribute.attributeId)
        aSoapObject.addProperty("attribute_type_id", attribute.attributeTypeId)
        aSoapObject.addProperty("description", attribute.description)
        aSoapObject.addProperty("active", attribute.active)
        aSoapObject.addProperty("attribute_category_id", attribute.attributeCategoryId)

        val result = getWebservice().s(
            methodName = "Attribute_Add",
            params = arrayOf(WsParam("user_id", userId)),
            soapObjParams1 = aSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun attributeCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "Attribute_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}