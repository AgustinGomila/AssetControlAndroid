package com.example.assetControl.data.webservice.maintenance

import com.example.assetControl.data.webservice.common.Webservice.Companion.getMainWebservice
import com.example.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class MaintenanceTypeWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun maintenanceTypeGetAll(
    ): Array<MaintenanceTypeObject> {
        val any = getMainWebservice().s(
            "ManteinanceType_GetAll"
        )
        val dObjAl: ArrayList<MaintenanceTypeObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(MaintenanceTypeObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun maintenanceTypeGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<MaintenanceTypeObject> {
        val any = getMainWebservice().s(
            "ManteinanceType_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<MaintenanceTypeObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(MaintenanceTypeObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun maintenanceTypeModify(
        userId: Long,
        maintenanceType: MaintenanceTypeObject,
    ): Long {
        val waSoapObject = SoapObject(getMainWebservice().namespace, "manteinance_type")
        waSoapObject.addProperty("manteinance_type_id", maintenanceType.manteinance_type_id)
        waSoapObject.addProperty(
            "manteinance_type_group_id",
            maintenanceType.manteinance_type_group_id
        )
        waSoapObject.addProperty("description", maintenanceType.description)
        waSoapObject.addProperty("active", maintenanceType.active)

        val result = getMainWebservice().s(
            "ManteinanceType_Modify",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun maintenanceTypeAdd(
        userId: Long,
        maintenanceType: MaintenanceTypeObject,
    ): Long {
        val waSoapObject = SoapObject(getMainWebservice().namespace, "manteinance_type")
        waSoapObject.addProperty("manteinance_type_id", maintenanceType.manteinance_type_id)
        waSoapObject.addProperty(
            "manteinance_type_group_id",
            maintenanceType.manteinance_type_group_id
        )
        waSoapObject.addProperty("description", maintenanceType.description)
        waSoapObject.addProperty("active", maintenanceType.active)

        val result = getMainWebservice().s(
            "ManteinanceType_Add",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun maintenanceTypeCount(date: String): Int? {
        val result = getMainWebservice().s(
            methodName = "ManteinanceType_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}