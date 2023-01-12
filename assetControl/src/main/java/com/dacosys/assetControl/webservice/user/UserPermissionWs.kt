package com.dacosys.assetControl.webservice.user

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class UserPermissionWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun userPermissionGet(
        userId: Long,
    ): Array<UserPermissionObject>? {
        val any = Statics.getWebservice().s(
            "UserPermission_Get",
            arrayOf(

                WsParam("user_id", userId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<UserPermissionObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(UserPermissionObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun userPermissionAdd(
        userId: Long,
        user: UserObject,
        userPermission: ArrayList<UserPermissionObject>,
    ): Long {
        val userSoapObject = SoapObject(Statics.getWebservice().namespace, "user")
        userSoapObject.addProperty("user_id", user.user_id)
        userSoapObject.addProperty("name", user.name)
        userSoapObject.addProperty("active", user.active)
        userSoapObject.addProperty("password", user.password)

        val permissionArrayObject = SoapObject(Statics.getWebservice().namespace, "permission")
        userPermission.forEach { t ->
            val upObject = SoapObject(Statics.getWebservice().namespace, "UserPermissionObject")
            upObject.addProperty("user_id", t.user_id)
            upObject.addProperty("permission_id", t.permission_id)

            permissionArrayObject.addSoapObject(upObject)
        }

        val result = Statics.getWebservice().s(
            "User_Permission_Add",
            arrayOf(WsParam("user_id", userId)),
            userSoapObject,
            arrayOf(permissionArrayObject)

        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun userPermissionDeleteAll(
        userId: Int,
    ): Int {
        val result = Statics.getWebservice().s(
            "UserPermission_Delete_All",
            arrayOf(WsParam("user_id", userId))
        ) ?: return 0
        return result as Int
    }

    @Throws(Exception::class)
    fun userPermissionDeleteAllInterval(
        userId: Long,
        max: Int,
        min: Int,
    ): Boolean {
        val result = Statics.getWebservice().s(
            "UserPermission_Delete_All_Interval",
            arrayOf(
                WsParam("user_id", userId),
                WsParam("max", max),
                WsParam("min", min)
            )
        ) ?: return false
        return result as Boolean
    }

    @Throws(Exception::class)
    fun initialUserPermissionGet(
        userId: Long,
    ): Array<UserPermissionObject>? {
        val any = Statics.getWebservice().s(
            "UserPermission_Get",
            arrayOf(

                WsParam("user_id", userId)
            ),
            null,
            null,
            true
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<UserPermissionObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(UserPermissionObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }
}