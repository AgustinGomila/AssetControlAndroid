package com.dacosys.assetControl.data.webservice.user

import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class UserWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun userGetAll(
    ): Array<UserObject> {
        val any = getWebservice().s(
            "User_GetAll"
        )
        val dObjAl: ArrayList<UserObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(UserObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun userGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<UserObject> {
        val any = getWebservice().s(
            "User_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<UserObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(UserObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun userModify(
        userId: Long,
        user: UserObject,
    ): Long {
        val uSoapObject = SoapObject(getWebservice().namespace, "user")
        uSoapObject.addProperty("user_id", user.user_id)
        uSoapObject.addProperty("name", user.name)
        uSoapObject.addProperty("active", user.active)
        uSoapObject.addProperty("password", user.password)

        val result = getWebservice().s(
            "User_Modify",
            arrayOf(WsParam("user_id", userId)),
            uSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun userAdd(
        userId: Long,
        user: UserObject,
    ): Long {
        val uSoapObject = SoapObject(getWebservice().namespace, "user")
        uSoapObject.addProperty("user_id", user.user_id)
        uSoapObject.addProperty("name", user.name)
        uSoapObject.addProperty("active", user.active)
        uSoapObject.addProperty("password", user.password)

        val result = getWebservice().s(
            "User_Add",
            arrayOf(WsParam("user_id", userId)),
            uSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun userCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "User_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }

    @Throws(Exception::class)
    fun initialUserCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "User_Collector_Count",
            params = arrayOf(WsParam("date", date)),
            soapObjParams1 = null,
            soapObjParams2 = null,
            useConfSession = true
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }

    @Throws(Exception::class)
    fun initialUserGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<UserObject> {
        val any = getWebservice().s(
            "User_GetCollector_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            ),
            null,
            null,
            true
        )
        val dObjAl: ArrayList<UserObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(UserObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }
}