package com.dacosys.assetControl.webservice.manteinance

import com.dacosys.assetControl.webservice.common.Webservice.Companion.getMantWebservice
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class ManteinanceTypeGroupWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun manteinanceTypeGroupGetAll(
    ): Array<ManteinanceTypeGroupObject> {
        val any = getMantWebservice().s(
            "Manteinance_Type_Group_GetAll"
        )
        val dObjAl: ArrayList<ManteinanceTypeGroupObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(ManteinanceTypeGroupObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun manteinanceTypeGroupGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<ManteinanceTypeGroupObject> {
        val any = getMantWebservice().s(
            "Manteinance_Type_Group_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<ManteinanceTypeGroupObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(ManteinanceTypeGroupObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun manteinanceTypeGroupModify(
        userId: Long,
        manteinanceTypeGroup: ManteinanceTypeGroupObject,
    ): Long {
        val acSoapObject =
            SoapObject(getMantWebservice().namespace, "manteinance_type_group")
        acSoapObject.addProperty(
            "manteinance_type_group_id",
            manteinanceTypeGroup.manteinanceTypeGroupId
        )
        acSoapObject.addProperty("description", manteinanceTypeGroup.description)
        acSoapObject.addProperty("active", manteinanceTypeGroup.active)

        val result = getMantWebservice().s(
            "Manteinance_Type_Group_Modify",
            arrayOf(WsParam("user_id", userId)),
            acSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun manteinanceTypeGroupAdd(
        userId: Long,
        manteinanceTypeGroup: ManteinanceTypeGroupObject,
    ): Long {
        val acSoapObject =
            SoapObject(getMantWebservice().namespace, "manteinance_type_group")
        acSoapObject.addProperty(
            "manteinance_type_group_id",
            manteinanceTypeGroup.manteinanceTypeGroupId
        )
        acSoapObject.addProperty("description", manteinanceTypeGroup.description)
        acSoapObject.addProperty("active", manteinanceTypeGroup.active)

        val result = getMantWebservice().s(
            "Manteinance_Type_Group_Add",
            arrayOf(WsParam("user_id", userId)),
            acSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun manteinanceTypeGroupCount(date: String): Int? {
        val result = getMantWebservice().s(
            methodName = "Manteinance_Type_Group_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}