package com.dacosys.assetControl.webservice.user

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class UserWarehouseAreaWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun userWarehouseAreaGet(
        userId: Long,
    ): Array<UserWarehouseAreaObject>? {
        val any = Statics.getWebservice().s(
            "UserWarehouseArea_Get",
            arrayOf(

                WsParam("user_id", userId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<UserWarehouseAreaObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(UserWarehouseAreaObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun userWarehouseAreaGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<UserWarehouseAreaObject>? {
        val any = Statics.getWebservice().s(
            "UserWarehouseArea_GetAll_Limit",
            arrayOf(

                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<UserWarehouseAreaObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(UserWarehouseAreaObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun userWarehouseAreaAdd(
        userId: Long,
        user: UserObject,
        userWarehouseArea: ArrayList<UserWarehouseAreaObject>,
    ): Long {
        val userSoapObject = SoapObject(Statics.getWebservice().namespace, "user")
        userSoapObject.addProperty("user_id", user.user_id)
        userSoapObject.addProperty("name", user.name)
        userSoapObject.addProperty("active", user.active)
        userSoapObject.addProperty("password", user.password)

        val warehouseAreaArrayObject =
            SoapObject(Statics.getWebservice().namespace, "warehouse_area")
        userWarehouseArea.forEach { t ->
            val upObject = SoapObject(Statics.getWebservice().namespace, "UserWarehouseAreaObject")
            upObject.addProperty("user_id", t.user_id)
            upObject.addProperty("warehouse_area_id", t.warehouse_area_id)
            upObject.addProperty("see", t.see)
            upObject.addProperty("move", t.move)
            upObject.addProperty("count", t.count)
            upObject.addProperty("check", t.check)

            warehouseAreaArrayObject.addSoapObject(upObject)
        }

        val result = Statics.getWebservice().s(
            "User_WarehouseArea_Add",
            arrayOf(WsParam("user_id", userId)),
            userSoapObject,
            arrayOf(warehouseAreaArrayObject)

        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun userWarehouseAreaDeleteAll(
        userId: Int,
    ): Int {
        val result = Statics.getWebservice().s(
            "UserWarehouseArea_Delete_All",
            arrayOf(WsParam("user_id", userId))
        ) ?: return 0
        return result as Int
    }

    @Throws(Exception::class)
    fun userWarehouseAreaCount(date: String): Int? {
        val result = Statics.getWebservice().s(
            methodName = "UserWarehouseArea_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }

    @Throws(Exception::class)
    fun initialUserWarehouseAreaGet(
        userId: Long,
    ): Array<UserWarehouseAreaObject>? {
        val any = Statics.getWebservice().s(
            "UserWarehouseArea_Get",
            arrayOf(

                WsParam("user_id", userId)
            ),
            null,
            null,
            true
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<UserWarehouseAreaObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(UserWarehouseAreaObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }
}