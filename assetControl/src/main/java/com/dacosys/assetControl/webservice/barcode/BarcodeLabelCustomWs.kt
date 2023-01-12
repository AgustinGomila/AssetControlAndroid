package com.dacosys.assetControl.webservice.barcode

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class BarcodeLabelCustomWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun barcodeLabelCustomGetAll(
    ): Array<BarcodeLabelCustomObject> {
        val any = Statics.getWebservice().s(
            "BarcodeLabelCustom_GetAll"
        )
        val dObjAl: ArrayList<BarcodeLabelCustomObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(BarcodeLabelCustomObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun barcodeLabelCustomGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<BarcodeLabelCustomObject> {
        val any = Statics.getWebservice().s(
            "BarcodeLabelCustom_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<BarcodeLabelCustomObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(BarcodeLabelCustomObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun barcodeLabelCustomModify(
        userId: Long,
        barcodeLabelCustom: BarcodeLabelCustomObject,
    ): Long {
        val icSoapObject = SoapObject(
            Statics.getWebservice().namespace,
            "barcode_label_custom"
        )

        icSoapObject.addProperty(
            "barcode_label_custom_id",
            barcodeLabelCustom.barcode_label_custom_id
        )
        icSoapObject.addProperty(
            "barcode_label_target_id",
            barcodeLabelCustom.barcode_label_target_id
        )
        icSoapObject.addProperty(
            "description",
            barcodeLabelCustom.description
        )
        icSoapObject.addProperty(
            "active",
            barcodeLabelCustom.active
        )
        icSoapObject.addProperty(
            "template",
            barcodeLabelCustom.template
        )

        val result = Statics.getWebservice().s(
            "BarcodeLabelCustom_Modify",
            arrayOf(WsParam("user_id", userId)),
            icSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun barcodeLabelCustomAdd(
        userId: Long,
        barcodeLabelCustom: BarcodeLabelCustomObject,
    ): Long {
        val icSoapObject = SoapObject(
            Statics.getWebservice().namespace,
            "barcode_label_custom"
        )

        icSoapObject.addProperty(
            "barcode_label_custom_id",
            barcodeLabelCustom.barcode_label_custom_id
        )
        icSoapObject.addProperty(
            "barcode_label_target_id",
            barcodeLabelCustom.barcode_label_target_id
        )
        icSoapObject.addProperty(
            "description",
            barcodeLabelCustom.description
        )
        icSoapObject.addProperty(
            "active",
            barcodeLabelCustom.active
        )
        icSoapObject.addProperty(
            "template",
            barcodeLabelCustom.template
        )

        val result = Statics.getWebservice().s(
            "BarcodeLabelCustom_Add",
            arrayOf(WsParam("user_id", userId)),
            icSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun barcodeLabelCustomCount(date: String): Int? {
        val result = Statics.getWebservice().s(
            methodName = "BarcodeLabelCustom_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}