package com.example.assetControl.data.webservice.category

import com.example.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.example.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class ItemCategoryWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun itemCategoryGetAll(
    ): Array<ItemCategoryObject> {
        val any = getWebservice().s(
            "ItemCategory_GetAll"
        )
        val dObjAl: ArrayList<ItemCategoryObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(ItemCategoryObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun itemCategoryGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<ItemCategoryObject> {
        val any = getWebservice().s(
            "ItemCategory_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<ItemCategoryObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(ItemCategoryObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun itemCategoryModify(
        userId: Long,
        itemCategory: ItemCategoryObject,
    ): Long {
        val icSoapObject = SoapObject(getWebservice().namespace, "item_category")
        icSoapObject.addProperty("item_category_id", itemCategory.item_category_id)
        icSoapObject.addProperty("parent_id", itemCategory.parent_id)
        icSoapObject.addProperty("description", itemCategory.description)
        icSoapObject.addProperty("active", itemCategory.active)
        icSoapObject.addProperty("item_category_ext_id", itemCategory.item_category_ext_id)

        val result = getWebservice().s(
            "ItemCategory_Modify",
            arrayOf(WsParam("user_id", userId)),
            icSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun itemCategoryAdd(
        userId: Long,
        itemCategory: ItemCategoryObject,
    ): Long {
        val icSoapObject = SoapObject(getWebservice().namespace, "item_category")
        icSoapObject.addProperty("item_category_id", itemCategory.item_category_id)
        icSoapObject.addProperty("parent_id", itemCategory.parent_id)
        icSoapObject.addProperty("description", itemCategory.description)
        icSoapObject.addProperty("active", itemCategory.active)
        icSoapObject.addProperty("item_category_ext_id", itemCategory.item_category_ext_id)

        val result = getWebservice().s(
            "ItemCategory_Add",
            arrayOf(WsParam("user_id", userId)),
            icSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun itemCategoryCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "ItemCategory_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}