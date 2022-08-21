package com.dacosys.assetControl.model.assets.manteinances.manteinanceType.wsObject

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.wsGeneral.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class ManteinanceTypeWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun manteinanceTypeGetAll(
    ): Array<ManteinanceTypeObject> {
        val any = Statics.getMantWebservice().s(
            "ManteinanceType_GetAll"
        )
        val dObjAl: ArrayList<ManteinanceTypeObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(ManteinanceTypeObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun manteinanceTypeGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<ManteinanceTypeObject> {
        val any = Statics.getMantWebservice().s(
            "ManteinanceType_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<ManteinanceTypeObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(ManteinanceTypeObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun manteinanceTypeModify(
        userId: Long,
        manteinanceType: ManteinanceTypeObject,
    ): Long {
        val waSoapObject = SoapObject(Statics.getMantWebservice().namespace, "manteinance_type")
        waSoapObject.addProperty("manteinance_type_id", manteinanceType.manteinance_type_id)
        waSoapObject.addProperty(
            "manteinance_type_group_id",
            manteinanceType.manteinance_type_group_id
        )
        waSoapObject.addProperty("description", manteinanceType.description)
        waSoapObject.addProperty("active", manteinanceType.active)

        val result = Statics.getMantWebservice().s(
            "ManteinanceType_Modify",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun manteinanceTypeAdd(
        userId: Long,
        manteinanceType: ManteinanceTypeObject,
    ): Long {
        val waSoapObject = SoapObject(Statics.getMantWebservice().namespace, "manteinance_type")
        waSoapObject.addProperty("manteinance_type_id", manteinanceType.manteinance_type_id)
        waSoapObject.addProperty(
            "manteinance_type_group_id",
            manteinanceType.manteinance_type_group_id
        )
        waSoapObject.addProperty("description", manteinanceType.description)
        waSoapObject.addProperty("active", manteinanceType.active)

        val result = Statics.getMantWebservice().s(
            "ManteinanceType_Add",
            arrayOf(WsParam("user_id", userId)),
            waSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun manteinanceTypeCount(date: String): Int? {
        val result = Statics.getMantWebservice().s(
            methodName = "ManteinanceType_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}