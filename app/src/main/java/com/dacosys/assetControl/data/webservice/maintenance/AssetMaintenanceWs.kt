package com.dacosys.assetControl.data.webservice.maintenance

import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getMainWebservice
import com.dacosys.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class AssetMaintenanceWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun assetMaintenanceGetAll(
    ): Array<AssetMaintenanceObject>? {
        val any = getMainWebservice().s("Asset_Manteinance_GetAll")
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<AssetMaintenanceObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(AssetMaintenanceObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun assetMaintenanceGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<AssetMaintenanceObject>? {
        val any = getMainWebservice().s(
            "Asset_Manteinance_GetAll_Limit_Collector",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<AssetMaintenanceObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(AssetMaintenanceObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun assetMaintenanceModify(
        userId: Long,
        assetMaintenance: AssetMaintenanceObject,
        assetMaintenanceLog: AssetMaintenanceLogObject,
    ): Long {
        /*
        user_id: xsd:int
        asset_manteinance: tns:AssetMaintenanceObject
        asset_manteinance_log: tns:AssetMaintenanceLogObject
        */

        /*
        <xsd:complexType name="AssetMaintenanceObject">

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
        <xsd:complexType name="AssetMaintenanceLogObject">

        <xsd:element name="asset_manteinance_id" type="xsd:int"/>
        <xsd:element name="manteinance_status_id" type="xsd:int"/>
        <xsd:element name="description" type="xsd:string"/>
        <xsd:element name="log_date" type="xsd:string"/>
        <xsd:element name="repairshop_id" type="xsd:int"/>
        <xsd:element name="repairman_id" type="xsd:int"/>
        */

        val icSoapObject = SoapObject(getMainWebservice().namespace, "asset_manteinance")
        icSoapObject.addProperty("asset_manteinance_id", assetMaintenance.asset_manteinance_id)
        icSoapObject.addProperty("manteinance_status_id", assetMaintenance.manteinance_status_id)
        //icSoapObject.addProperty("manteinance_start_date", assetManteinance.manteinance_start_date)
        //icSoapObject.addProperty("manteinance_completed_date", assetManteinance.manteinance_completed_date)
        //icSoapObject.addProperty("asset_reception_date", assetManteinance.asset_reception_date)
        //icSoapObject.addProperty("repair_cost", assetManteinance.repair_cost)
        //icSoapObject.addProperty("invoice", assetManteinance.invoice)
        icSoapObject.addProperty("asset_id", assetMaintenance.asset_id)
        icSoapObject.addProperty("repairman_id", assetMaintenance.repairman_id)
        //icSoapObject.addProperty("repairshop_id", assetManteinance.repairshop_id)
        //icSoapObject.addProperty("repairshop_area_id", assetManteinance.repairshop_area_id)
        //icSoapObject.addProperty("repairshop_assigned_id", assetManteinance.repairshop_assigned_id)
        //icSoapObject.addProperty("repairshop_area_assigned_id", assetManteinance.repairshop_area_assigned_id)
        //icSoapObject.addProperty("estimated_repair_date", assetManteinance.estimated_repair_date)
        //icSoapObject.addProperty("completed", assetManteinance.completed)
        //icSoapObject.addProperty("original_warehouse_area_id", assetManteinance.original_warehouse_area_id)
        //icSoapObject.addProperty("original_warehouse_id", assetManteinance.original_warehouse_id)
        //icSoapObject.addProperty("priority", assetManteinance.priority)
        icSoapObject.addProperty("manteinance_type_id", assetMaintenance.manteinance_type_id)

        val logSoapObject =
            SoapObject(getMainWebservice().namespace, "asset_manteinance_log")
        logSoapObject.addProperty("asset_manteinance_id", assetMaintenanceLog.asset_manteinance_id)
        logSoapObject.addProperty(
            "manteinance_status_id",
            assetMaintenanceLog.manteinance_status_id
        )
        logSoapObject.addProperty("description", assetMaintenanceLog.description)
        logSoapObject.addProperty("log_date", assetMaintenanceLog.log_date)
        logSoapObject.addProperty("repairshop_id", assetMaintenanceLog.repairshop_id)
        logSoapObject.addProperty("repairman_id", assetMaintenanceLog.repairman_id)

        val soapObjectAl: ArrayList<SoapObject> = ArrayList()
        soapObjectAl.add(icSoapObject)
        soapObjectAl.add(logSoapObject)

        val result = getMainWebservice().s(
            "Asset_Manteinance_Modify_Collector",
            arrayOf(WsParam("user_id", userId)),
            soapObjectAl.toTypedArray()
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun assetMaintenanceAdd(
        userId: Long,
        assetMaintenance: AssetMaintenanceObject,
        assetMaintenanceLog: AssetMaintenanceLogObject,
    ): Long {
        /*
        user_id: xsd:int
        asset_manteinance: tns:AssetMaintenanceObject
        asset_manteinance_log: tns:AssetMaintenanceLogObject
        */

        val icSoapObject = SoapObject(getMainWebservice().namespace, "asset_manteinance")
        icSoapObject.addProperty("asset_manteinance_id", assetMaintenance.asset_manteinance_id)
        icSoapObject.addProperty("manteinance_status_id", assetMaintenance.manteinance_status_id)
        //icSoapObject.addProperty("manteinance_start_date", assetManteinance.manteinance_start_date)
        //icSoapObject.addProperty("manteinance_completed_date", assetManteinance.manteinance_completed_date)
        //icSoapObject.addProperty("asset_reception_date", assetManteinance.asset_reception_date)
        //icSoapObject.addProperty("repair_cost", assetManteinance.repair_cost)
        //icSoapObject.addProperty("invoice", assetManteinance.invoice)
        icSoapObject.addProperty("asset_id", assetMaintenance.asset_id)
        icSoapObject.addProperty("repairman_id", assetMaintenance.repairman_id)
        //icSoapObject.addProperty("repairshop_id", assetManteinance.repairshop_id)
        //icSoapObject.addProperty("repairshop_area_id", assetManteinance.repairshop_area_id)
        //icSoapObject.addProperty("repairshop_assigned_id", assetManteinance.repairshop_assigned_id)
        //icSoapObject.addProperty("repairshop_area_assigned_id", assetManteinance.repairshop_area_assigned_id)
        //icSoapObject.addProperty("estimated_repair_date", assetManteinance.estimated_repair_date)
        //icSoapObject.addProperty("completed", assetManteinance.completed)
        //icSoapObject.addProperty("original_warehouse_area_id", assetManteinance.original_warehouse_area_id)
        //icSoapObject.addProperty("original_warehouse_id", assetManteinance.original_warehouse_id)
        //icSoapObject.addProperty("priority", assetManteinance.priority)
        icSoapObject.addProperty("manteinance_type_id", assetMaintenance.manteinance_type_id)

        val logSoapObject =
            SoapObject(getMainWebservice().namespace, "asset_manteinance_log")
        logSoapObject.addProperty("asset_manteinance_id", assetMaintenanceLog.asset_manteinance_id)
        logSoapObject.addProperty(
            "manteinance_status_id",
            assetMaintenanceLog.manteinance_status_id
        )
        logSoapObject.addProperty("description", assetMaintenanceLog.description)
        logSoapObject.addProperty("log_date", assetMaintenanceLog.log_date)
        logSoapObject.addProperty("repairshop_id", assetMaintenanceLog.repairshop_id)
        logSoapObject.addProperty("repairman_id", assetMaintenanceLog.repairman_id)

        val soapObjectAl: ArrayList<SoapObject> = ArrayList()
        soapObjectAl.add(icSoapObject)
        soapObjectAl.add(logSoapObject)

        val result = getMainWebservice().s(
            "Asset_Manteinance_Add_Collector",
            arrayOf(WsParam("user_id", userId)),
            soapObjectAl.toTypedArray()
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun assetMaintenanceCount(
        date: String,
    ): Int {
        val result = getMainWebservice().s(
            "Asset_Manteinance_Count_Collector",
            arrayOf(WsParam("date", date))
        ) ?: return 0
        return result as Int
    }
}