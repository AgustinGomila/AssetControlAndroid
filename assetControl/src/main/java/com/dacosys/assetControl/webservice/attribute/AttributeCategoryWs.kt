package com.dacosys.assetControl.webservice.attribute

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class AttributeCategoryWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun attributeCategoryGetAll(
    ): Array<AttributeCategoryObject> {
        val any = Statics.getWebservice().s(
            "AttributeCategory_GetAll"
        )
        val dObjAl: ArrayList<AttributeCategoryObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(AttributeCategoryObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun attributeCategoryGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<AttributeCategoryObject> {
        val any = Statics.getWebservice().s(
            "AttributeCategory_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<AttributeCategoryObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(AttributeCategoryObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun attributeCategoryModify(
        userId: Long,
        attributeCategory: AttributeCategoryObject,
    ): Long {
        val acSoapObject = SoapObject(Statics.getWebservice().namespace, "attribute_category")
        acSoapObject.addProperty("attribute_category_id", attributeCategory.attributeCategoryId)
        acSoapObject.addProperty("parent_id", attributeCategory.parentId)
        acSoapObject.addProperty("description", attributeCategory.description)
        acSoapObject.addProperty("active", attributeCategory.active)

        val result = Statics.getWebservice().s(
            "AttributeCategory_Modify",
            arrayOf(WsParam("user_id", userId)),
            acSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun attributeCategoryAdd(
        userId: Long,
        attributeCategory: AttributeCategoryObject,
    ): Long {
        val acSoapObject = SoapObject(Statics.getWebservice().namespace, "attribute_category")
        acSoapObject.addProperty("attribute_category_id", attributeCategory.attributeCategoryId)
        acSoapObject.addProperty("parent_id", attributeCategory.parentId)
        acSoapObject.addProperty("description", attributeCategory.description)
        acSoapObject.addProperty("active", attributeCategory.active)

        val result = Statics.getWebservice().s(
            "AttributeCategory_Add",
            arrayOf(WsParam("user_id", userId)),
            acSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun attributeCategoryCount(date: String): Int? {
        val result = Statics.getWebservice().s(
            methodName = "AttributeCategory_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}