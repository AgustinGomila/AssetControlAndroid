package com.example.assetControl.data.webservice.maintenance

import com.example.assetControl.data.webservice.common.Webservice.Companion.getMainWebservice
import com.example.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class ManteinanceTypeGroupWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun manteinanceTypeGroupGetAll(
    ): Array<MaintenanceTypeGroupObject> {
        val any = getMainWebservice().s(
            "Manteinance_Type_Group_GetAll"
        )
        val dObjAl: ArrayList<MaintenanceTypeGroupObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(MaintenanceTypeGroupObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun manteinanceTypeGroupGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<MaintenanceTypeGroupObject> {
        val any = getMainWebservice().s(
            "Manteinance_Type_Group_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<MaintenanceTypeGroupObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(MaintenanceTypeGroupObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun manteinanceTypeGroupModify(
        userId: Long,
        manteinanceTypeGroup: MaintenanceTypeGroupObject,
    ): Long {
        val acSoapObject =
            SoapObject(getMainWebservice().namespace, "manteinance_type_group")
        acSoapObject.addProperty(
            "manteinance_type_group_id",
            manteinanceTypeGroup.maintenanceTypeGroupId
        )
        acSoapObject.addProperty("description", manteinanceTypeGroup.description)
        acSoapObject.addProperty("active", manteinanceTypeGroup.active)

        val result = getMainWebservice().s(
            "Manteinance_Type_Group_Modify",
            arrayOf(WsParam("user_id", userId)),
            acSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun manteinanceTypeGroupAdd(
        userId: Long,
        manteinanceTypeGroup: MaintenanceTypeGroupObject,
    ): Long {
        val acSoapObject =
            SoapObject(getMainWebservice().namespace, "manteinance_type_group")
        acSoapObject.addProperty(
            "manteinance_type_group_id",
            manteinanceTypeGroup.maintenanceTypeGroupId
        )
        acSoapObject.addProperty("description", manteinanceTypeGroup.description)
        acSoapObject.addProperty("active", manteinanceTypeGroup.active)

        val result = getMainWebservice().s(
            "Manteinance_Type_Group_Add",
            arrayOf(WsParam("user_id", userId)),
            acSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun manteinanceTypeGroupCount(date: String): Int? {
        val result = getMainWebservice().s(
            methodName = "Manteinance_Type_Group_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}