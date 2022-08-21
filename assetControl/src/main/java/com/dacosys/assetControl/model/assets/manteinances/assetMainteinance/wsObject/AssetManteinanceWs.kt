package com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.wsObject

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.wsGeneral.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class AssetManteinanceWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun assetManteinanceGetAll(
    ): Array<AssetManteinanceObject>? {
        val any = Statics.getMantWebservice().s("Asset_Manteinance_GetAll")
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<AssetManteinanceObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(AssetManteinanceObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun assetManteinanceGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<AssetManteinanceObject>? {
        val any = Statics.getMantWebservice().s(
            "Asset_Manteinance_GetAll_Limit_Collector",
            arrayOf(

                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<AssetManteinanceObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(AssetManteinanceObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun assetManteinanceModify(
        userId: Long,
        assetManteinance: AssetManteinanceObject,
        assetManteinanceLog: AssetManteinanceLogObject,
    ): Long {
        /*
        user_id: xsd:int
        asset_manteinance: tns:AssetManteinanceObject
        asset_manteinance_log: tns:AssetManteinanceLogObject
        */

        /*
        <xsd:complexType name="AssetManteinanceObject">

        <xsd:element name="asset_manteinance_id" type="xsd:int"/>
        <xsd:element name="manteinance_status_id" type="xsd:int"/>
        <xsd:element name="manteinance_start_date" type="xsd:string"/>
        <xsd:element name="manteinance_completed_date" type="xsd:string"/>
        <xsd:element name="asset_reception_date" type="xsd:string"/>
        <xsd:element name="repair_cost" type="xsd:decimal"/>
        <xsd:element name="invoice" type="xsd:string"/>
        <xsd:element name="asset_id" type="xsd:int"/>
        <xsd:element name="repairman_id" type="xsd:int"/>
        <xsd:element name="repairshop_id" type="xsd:int"/>
        <xsd:element name="repairshop_area_id" type="xsd:int"/>
        <xsd:element name="repairshop_assigned_id" type="xsd:int"/>
        <xsd:element name="repairshop_area_assigned_id" type="xsd:int"/>
        <xsd:element name="estimated_repair_date" type="xsd:string"/>
        <xsd:element name="completed" type="xsd:int"/>
        <xsd:element name="original_warehouse_area_id" type="xsd:int"/>
        <xsd:element name="original_warehouse_id" type="xsd:int"/>
        <xsd:element name="priority" type="xsd:int"/>
        <xsd:element name="manteinance_type_id" type="xsd:int"/>
        */

        /*
        <xsd:complexType name="AssetManteinanceLogObject">

        <xsd:element name="asset_manteinance_id" type="xsd:int"/>
        <xsd:element name="manteinance_status_id" type="xsd:int"/>
        <xsd:element name="description" type="xsd:string"/>
        <xsd:element name="log_date" type="xsd:string"/>
        <xsd:element name="repairshop_id" type="xsd:int"/>
        <xsd:element name="repairman_id" type="xsd:int"/>
        */

        val icSoapObject = SoapObject(Statics.getMantWebservice().namespace, "asset_manteinance")
        icSoapObject.addProperty("asset_manteinance_id", assetManteinance.asset_manteinance_id)
        icSoapObject.addProperty("manteinance_status_id", assetManteinance.manteinance_status_id)
        //icSoapObject.addProperty("manteinance_start_date", assetManteinance.manteinance_start_date)
        //icSoapObject.addProperty("manteinance_completed_date", assetManteinance.manteinance_completed_date)
        //icSoapObject.addProperty("asset_reception_date", assetManteinance.asset_reception_date)
        //icSoapObject.addProperty("repair_cost", assetManteinance.repair_cost)
        //icSoapObject.addProperty("invoice", assetManteinance.invoice)
        icSoapObject.addProperty("asset_id", assetManteinance.asset_id)
        icSoapObject.addProperty("repairman_id", assetManteinance.repairman_id)
        //icSoapObject.addProperty("repairshop_id", assetManteinance.repairshop_id)
        //icSoapObject.addProperty("repairshop_area_id", assetManteinance.repairshop_area_id)
        //icSoapObject.addProperty("repairshop_assigned_id", assetManteinance.repairshop_assigned_id)
        //icSoapObject.addProperty("repairshop_area_assigned_id", assetManteinance.repairshop_area_assigned_id)
        //icSoapObject.addProperty("estimated_repair_date", assetManteinance.estimated_repair_date)
        //icSoapObject.addProperty("completed", assetManteinance.completed)
        //icSoapObject.addProperty("original_warehouse_area_id", assetManteinance.original_warehouse_area_id)
        //icSoapObject.addProperty("original_warehouse_id", assetManteinance.original_warehouse_id)
        //icSoapObject.addProperty("priority", assetManteinance.priority)
        icSoapObject.addProperty("manteinance_type_id", assetManteinance.manteinance_type_id)

        val logSoapObject =
            SoapObject(Statics.getMantWebservice().namespace, "asset_manteinance_log")
        logSoapObject.addProperty("asset_manteinance_id", assetManteinanceLog.asset_manteinance_id)
        logSoapObject.addProperty(
            "manteinance_status_id",
            assetManteinanceLog.manteinance_status_id
        )
        logSoapObject.addProperty("description", assetManteinanceLog.description)
        logSoapObject.addProperty("log_date", assetManteinanceLog.log_date)
        logSoapObject.addProperty("repairshop_id", assetManteinanceLog.repairshop_id)
        logSoapObject.addProperty("repairman_id", assetManteinanceLog.repairman_id)

        val soapObjectAl: ArrayList<SoapObject> = ArrayList()
        soapObjectAl.add(icSoapObject)
        soapObjectAl.add(logSoapObject)

        val result = Statics.getMantWebservice().s(
            "Asset_Manteinance_Modify_Collector",
            arrayOf(WsParam("user_id", userId)),
            soapObjectAl.toTypedArray()
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun assetManteinanceAdd(
        userId: Long,
        assetManteinance: AssetManteinanceObject,
        assetManteinanceLog: AssetManteinanceLogObject,
    ): Long {
        /*
        user_id: xsd:int
        asset_manteinance: tns:AssetManteinanceObject
        asset_manteinance_log: tns:AssetManteinanceLogObject
        */

        val icSoapObject = SoapObject(Statics.getMantWebservice().namespace, "asset_manteinance")
        icSoapObject.addProperty("asset_manteinance_id", assetManteinance.asset_manteinance_id)
        icSoapObject.addProperty("manteinance_status_id", assetManteinance.manteinance_status_id)
        //icSoapObject.addProperty("manteinance_start_date", assetManteinance.manteinance_start_date)
        //icSoapObject.addProperty("manteinance_completed_date", assetManteinance.manteinance_completed_date)
        //icSoapObject.addProperty("asset_reception_date", assetManteinance.asset_reception_date)
        //icSoapObject.addProperty("repair_cost", assetManteinance.repair_cost)
        //icSoapObject.addProperty("invoice", assetManteinance.invoice)
        icSoapObject.addProperty("asset_id", assetManteinance.asset_id)
        icSoapObject.addProperty("repairman_id", assetManteinance.repairman_id)
        //icSoapObject.addProperty("repairshop_id", assetManteinance.repairshop_id)
        //icSoapObject.addProperty("repairshop_area_id", assetManteinance.repairshop_area_id)
        //icSoapObject.addProperty("repairshop_assigned_id", assetManteinance.repairshop_assigned_id)
        //icSoapObject.addProperty("repairshop_area_assigned_id", assetManteinance.repairshop_area_assigned_id)
        //icSoapObject.addProperty("estimated_repair_date", assetManteinance.estimated_repair_date)
        //icSoapObject.addProperty("completed", assetManteinance.completed)
        //icSoapObject.addProperty("original_warehouse_area_id", assetManteinance.original_warehouse_area_id)
        //icSoapObject.addProperty("original_warehouse_id", assetManteinance.original_warehouse_id)
        //icSoapObject.addProperty("priority", assetManteinance.priority)
        icSoapObject.addProperty("manteinance_type_id", assetManteinance.manteinance_type_id)

        val logSoapObject =
            SoapObject(Statics.getMantWebservice().namespace, "asset_manteinance_log")
        logSoapObject.addProperty("asset_manteinance_id", assetManteinanceLog.asset_manteinance_id)
        logSoapObject.addProperty(
            "manteinance_status_id",
            assetManteinanceLog.manteinance_status_id
        )
        logSoapObject.addProperty("description", assetManteinanceLog.description)
        logSoapObject.addProperty("log_date", assetManteinanceLog.log_date)
        logSoapObject.addProperty("repairshop_id", assetManteinanceLog.repairshop_id)
        logSoapObject.addProperty("repairman_id", assetManteinanceLog.repairman_id)

        val soapObjectAl: ArrayList<SoapObject> = ArrayList()
        soapObjectAl.add(icSoapObject)
        soapObjectAl.add(logSoapObject)

        val result = Statics.getMantWebservice().s(
            "Asset_Manteinance_Add_Collector",
            arrayOf(WsParam("user_id", userId)),
            soapObjectAl.toTypedArray()
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun assetManteinanceCount(
        date: String,
    ): Int {
        val result = Statics.getMantWebservice().s(
            "Asset_Manteinance_Count_Collector",
            arrayOf(WsParam("date", date))
        ) ?: return 0
        return result as Int
    }
}