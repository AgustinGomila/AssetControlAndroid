package com.dacosys.assetControl.data.webservice.asset

import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class AssetWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun assetCollectorGetAll(
    ): Array<AssetCollectorObject> {
        val any = getWebservice().s("Asset_Collector_GetAll")
        val dObjAl: ArrayList<AssetCollectorObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(AssetCollectorObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun assetCollectorGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<AssetCollectorObject> {
        val any = getWebservice().s(
            "Asset_Collector_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<AssetCollectorObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(AssetCollectorObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun assetGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<AssetObject> {
        val any = getWebservice().s(
            "Asset_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        val dObjAl: ArrayList<AssetObject> = ArrayList()
        if (any != null) {
            val soVector = any as Vector<*>
            for (soDc in soVector) {
                if (soDc !is SoapObject) continue
                dObjAl.add(AssetObject().getBySoapObject(soDc))
            }
        }
        return dObjAl.toTypedArray()
    }

    @Throws(Exception::class)
    fun assetCollectorModify(
        userId: Long,
        asset: AssetCollectorObject,
    ): Long {
        val assetSoapObject = SoapObject(getWebservice().namespace, "asset")
        assetSoapObject.addProperty("asset_id", asset.asset_id)
        assetSoapObject.addProperty("parent_id", asset.parent_id)
        assetSoapObject.addProperty("code", asset.code)
        assetSoapObject.addProperty("warehouse_id", asset.warehouse_id)
        assetSoapObject.addProperty("warehouse_area_id", asset.warehouse_area_id)
        assetSoapObject.addProperty("active", asset.active)
        assetSoapObject.addProperty("serial_number", asset.serial_number)
        assetSoapObject.addProperty("ownership_status", asset.ownership_status)
        assetSoapObject.addProperty("status", asset.status)
        assetSoapObject.addProperty("missing_date", asset.missing_date)
        assetSoapObject.addProperty("description", asset.description)
        assetSoapObject.addProperty("ean", asset.ean)
        assetSoapObject.addProperty("item_category_id", asset.item_category_id)
        assetSoapObject.addProperty("original_warehouse_id", asset.original_warehouse_id)
        assetSoapObject.addProperty("original_warehouse_area_id", asset.original_warehouse_area_id)
        assetSoapObject.addProperty("label_number", asset.label_number)
        assetSoapObject.addProperty("condition", asset.condition)
        assetSoapObject.addProperty("last_asset_review_date", asset.last_asset_review_date)

        val result = getWebservice().s(
            "Asset_Collector_Modify",
            arrayOf(WsParam("user_id", userId)),
            assetSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun assetAdd(
        userId: Long,
        asset: AssetObject,
    ): Long {
        val assetSoapObject = SoapObject(getWebservice().namespace, "asset")

        /*
        val assetIdPi = PropertyInfo()
        assetIdPi.setKey("asset_id")
        assetIdPi.value = asset.asset_id
        assetIdPi.setType(Int::class.java)

        val parentIdPi = PropertyInfo()
        parentIdPi.setKey("parent_id")
        parentIdPi.value = asset.parent_id
        parentIdPi.setType(Int::class.java)

        val codePi = PropertyInfo()
        codePi.setKey("code")
        codePi.value = asset.code
        codePi.setType(String::class.java)

        val pricePi = PropertyInfo()
        pricePi.setKey("price")
        pricePi.value = asset.price
        pricePi.setType(Double::class.java)

        val warehouseIdPi = PropertyInfo()
        warehouseIdPi.setKey("warehouse_id")
        warehouseIdPi.value = asset.warehouse_id
        warehouseIdPi.setType(Int::class.java)

        val warehouseAreaIdPi = PropertyInfo()
        warehouseAreaIdPi.setKey("warehouse_area_id")
        warehouseAreaIdPi.value = asset.warehouse_area_id
        warehouseAreaIdPi.setType(Int::class.java)

        val activePi = PropertyInfo()
        activePi.setKey("active")
        activePi.value = asset.active
        activePi.setType(Int::class.java)

        val reasonPi = PropertyInfo()
        reasonPi.setKey("reason")
        reasonPi.value = asset.reason
        reasonPi.setType(String::class.java)

        val serialNumberPi = PropertyInfo()
        serialNumberPi.setKey("serial_number")
        serialNumberPi.value = asset.serial_number
        serialNumberPi.setType(String::class.java)

        val obsPi = PropertyInfo()
        obsPi.setKey("obs")
        obsPi.value = asset.obs
        obsPi.setType(String::class.java)

        val ownershipStatusPi = PropertyInfo()
        ownershipStatusPi.setKey("ownership_status")
        ownershipStatusPi.value = asset.ownership_status
        ownershipStatusPi.setType(Int::class.java)

        val warrantyDuePi = PropertyInfo()
        warrantyDuePi.setKey("warranty_due")
        warrantyDuePi.value = asset.warranty_due
        warrantyDuePi.setType(String::class.java)

        val statusPi = PropertyInfo()
        statusPi.setKey("status")
        statusPi.value = asset.status
        statusPi.setType(Int::class.java)

        val missingDatePi = PropertyInfo()
        missingDatePi.setKey("missing_date")
        missingDatePi.value = asset.missing_date
        missingDatePi.setType(String::class.java)

        val removedDatePi = PropertyInfo()
        removedDatePi.setKey("removed_date")
        removedDatePi.value = asset.removed_date
        removedDatePi.setType(String::class.java)

        val descriptionPi = PropertyInfo()
        descriptionPi.setKey("description")
        descriptionPi.value = asset.description
        descriptionPi.setType(String::class.java)

        val manufacturerPi = PropertyInfo()
        manufacturerPi.setKey("manufacturer")
        manufacturerPi.value = asset.manufacturer
        manufacturerPi.setType(String::class.java)

        val eanPi = PropertyInfo()
        eanPi.setKey("ean")
        eanPi.value = asset.ean
        eanPi.setType(String::class.java)

        val modelPi = PropertyInfo()
        modelPi.setKey("model")
        modelPi.value = asset.model
        modelPi.setType(String::class.java)

        val itemCategoryIdPi = PropertyInfo()
        itemCategoryIdPi.setKey("item_category_id")
        itemCategoryIdPi.value = asset.item_category_id
        itemCategoryIdPi.setType(Int::class.java)

        val costCentreIdPi = PropertyInfo()
        costCentreIdPi.setKey("cost_centre_id")
        costCentreIdPi.value = asset.cost_centre_id
        costCentreIdPi.setType(Int::class.java)

        val invoiceNrPi = PropertyInfo()
        invoiceNrPi.setKey("invoice_nr")
        invoiceNrPi.value = asset.invoice_nr
        invoiceNrPi.setType(String::class.java)

        val providerIdPi = PropertyInfo()
        providerIdPi.setKey("provider_id")
        providerIdPi.value = asset.provider_id
        providerIdPi.setType(Int::class.java)

        val purchaseDatePi = PropertyInfo()
        purchaseDatePi.setKey("purchase_date")
        purchaseDatePi.value = asset.purchase_date
        purchaseDatePi.setType(String::class.java)

        val amortizationMonthPi = PropertyInfo()
        amortizationMonthPi.setKey("amortization_month")
        amortizationMonthPi.value = asset.amortization_month
        amortizationMonthPi.setType(Int::class.java)

        val originalWarehouseIdPi = PropertyInfo()
        originalWarehouseIdPi.setKey("original_warehouse_id")
        originalWarehouseIdPi.value = asset.original_warehouse_id
        originalWarehouseIdPi.setType(Int::class.java)

        val originalWarehouseAreaIdPi = PropertyInfo()
        originalWarehouseAreaIdPi.setKey("original_warehouse_area_id")
        originalWarehouseAreaIdPi.value = asset.original_warehouse_area_id
        originalWarehouseAreaIdPi.setType(Int::class.java)

        val labelNumberPi = PropertyInfo()
        labelNumberPi.setKey("label_number")
        labelNumberPi.value = asset.label_number
        labelNumberPi.setType(Int::class.java)

        val insuranceCompanyPi = PropertyInfo()
        insuranceCompanyPi.setKey("insurance_company")
        insuranceCompanyPi.value = asset.insurance_company
        insuranceCompanyPi.setType(String::class.java)

        val insurancePolicyPi = PropertyInfo()
        insurancePolicyPi.setKey("insurance_policy")
        insurancePolicyPi.value = asset.insurance_policy
        insurancePolicyPi.setType(String::class.java)

        val autoInsurancePi = PropertyInfo()
        autoInsurancePi.setKey("auto_insurance")
        autoInsurancePi.value = asset.auto_insurance
        autoInsurancePi.setType(Int::class.java)

        val sensorMaticPi = PropertyInfo()
        sensorMaticPi.setKey("sensor_matic")
        sensorMaticPi.value = asset.sensor_matic
        sensorMaticPi.setType(Int::class.java)

        val rentalCostPi = PropertyInfo()
        rentalCostPi.setKey("rental_cost")
        rentalCostPi.value = asset.rental_cost
        rentalCostPi.setType(Double::class.java)

        val rentalPaymentModePi = PropertyInfo()
        rentalPaymentModePi.setKey("rental_payment_mode")
        rentalPaymentModePi.value = asset.rental_payment_mode
        rentalPaymentModePi.setType(Int::class.java)

        val leaseCostPi = PropertyInfo()
        leaseCostPi.setKey("lease_cost")
        leaseCostPi.value = asset.lease_cost
        leaseCostPi.setType(Double::class.java)

        val leasePaymentModePi = PropertyInfo()
        leasePaymentModePi.setKey("lease_payment_mode")
        leasePaymentModePi.value = asset.lease_payment_mode
        leasePaymentModePi.setType(Int::class.java)

        val leaseDatePi = PropertyInfo()
        leaseDatePi.setKey("lease_date")
        leaseDatePi.value = asset.lease_date
        leaseDatePi.setType(String::class.java)

        val leaseResidualValuePi = PropertyInfo()
        leaseResidualValuePi.setKey("lease_residual_value")
        leaseResidualValuePi.value = asset.lease_residual_value
        leaseResidualValuePi.setType(Double::class.java)

        val rentalDatePi = PropertyInfo()
        rentalDatePi.setKey("rental_date")
        rentalDatePi.value = asset.rental_date
        rentalDatePi.setType(String::class.java)

        val conditionPi = PropertyInfo()
        conditionPi.setKey("condition")
        conditionPi.value = asset.condition
        conditionPi.setType(Int::class.java)

        val assetExtIdPi = PropertyInfo()
        assetExtIdPi.setKey("asset_ext_id")
        assetExtIdPi.value = asset.asset_ext_id
        assetExtIdPi.setType(String::class.java)

        val lastAssetReviewDatePi = PropertyInfo()
        lastAssetReviewDatePi.setKey("last_asset_review_date")
        lastAssetReviewDatePi.value = asset.asset_ext_id
        lastAssetReviewDatePi.setType(String::class.java)

        assetSoapObject.addProperty(assetIdPi) //(asset.asset_id)
        assetSoapObject.addProperty(parentIdPi) //(asset.parent_id)
        assetSoapObject.addProperty(codePi) //(asset.code)
        assetSoapObject.addProperty(pricePi) //(asset.price)
        assetSoapObject.addProperty(warehouseIdPi) //(asset.warehouse_id)
        assetSoapObject.addProperty(warehouseAreaIdPi) //(asset.warehouse_area_id)
        assetSoapObject.addProperty(activePi) //(asset.active)
        assetSoapObject.addProperty(reasonPi) //(asset.reason)
        assetSoapObject.addProperty(serialNumberPi) //(asset.serial_number)
        assetSoapObject.addProperty(obsPi) //(asset.obs)
        assetSoapObject.addProperty(ownershipStatusPi) //(asset.ownership_status)
        assetSoapObject.addProperty(warrantyDuePi) //(asset.warranty_due)
        assetSoapObject.addProperty(statusPi) //(asset.status)
        assetSoapObject.addProperty(missingDatePi) //(asset.missing_date)
        assetSoapObject.addProperty(removedDatePi) //(asset.removed_date)
        assetSoapObject.addProperty(descriptionPi) //(asset.description)
        assetSoapObject.addProperty(manufacturerPi) //(asset.manufacturer)
        assetSoapObject.addProperty(eanPi) //(asset.ean)
        assetSoapObject.addProperty(modelPi) //(asset.model)
        assetSoapObject.addProperty(itemCategoryIdPi) //(asset.item_category_id)
        assetSoapObject.addProperty(costCentreIdPi) //(asset.cost_centre_id)
        assetSoapObject.addProperty(invoiceNrPi) //(asset.invoice_nr)
        assetSoapObject.addProperty(providerIdPi) //(asset.provider_id)
        assetSoapObject.addProperty(purchaseDatePi) //(asset.purchase_date)
        assetSoapObject.addProperty(amortizationMonthPi) //(asset.amortization_month)
        assetSoapObject.addProperty(originalWarehouseIdPi) //(asset.original_warehouse_id)
        assetSoapObject.addProperty(originalWarehouseAreaIdPi) //(asset.original_warehouse_area_id)
        assetSoapObject.addProperty(labelNumberPi) //(asset.label_number)
        assetSoapObject.addProperty(insuranceCompanyPi) //(asset.insurance_company)
        assetSoapObject.addProperty(insurancePolicyPi) //(asset.insurance_policy)
        assetSoapObject.addProperty(autoInsurancePi) //(asset.auto_insurance)
        assetSoapObject.addProperty(sensorMaticPi) //(asset.sensor_matic)
        assetSoapObject.addProperty(rentalCostPi) //(asset.rental_cost)
        assetSoapObject.addProperty(rentalPaymentModePi) //(asset.rental_payment_mode)
        assetSoapObject.addProperty(leaseCostPi) //("lease_costPi) //(asset.lease_cost)
        assetSoapObject.addProperty(leasePaymentModePi) //(asset.lease_payment_mode)
        assetSoapObject.addProperty(leaseDatePi) //(asset.lease_date)
        assetSoapObject.addProperty(leaseResidualValuePi) //(asset.lease_residual_value)
        assetSoapObject.addProperty(rentalDatePi) //(asset.rental_date)
        assetSoapObject.addProperty(conditionPi) //(asset.condition)
        assetSoapObject.addProperty(assetExtIdPi) //(asset.asset_ext_id)
        assetSoapObject.addProperty(lastAssetReviewDatePi) //(asset.last_asset_review_date)
        */

        /*
        <xsd:element name="asset_id" type="xsd:int"/>
        <xsd:element name="parent_id" type="xsd:int"/>
        <xsd:element name="code" type="xsd:string"/>
        <xsd:element name="price" type="xsd:decimal"/>
        <xsd:element name="warehouse_id" type="xsd:int"/>
        <xsd:element name="warehouse_area_id" type="xsd:int"/>
        <xsd:element name="active" type="xsd:int"/>
        <xsd:element name="reason" type="xsd:string"/>
        <xsd:element name="serial_number" type="xsd:string"/>
        <xsd:element name="obs" type="xsd:string"/>
        <xsd:element name="ownership_status" type="xsd:int"/>
        <xsd:element name="warranty_due" type="xsd:string"/>
        <xsd:element name="status" type="xsd:int"/>
        <xsd:element name="missing_date" type="xsd:string"/>
        <xsd:element name="removed_date" type="xsd:string"/>
        <xsd:element name="description" type="xsd:string"/>
        <xsd:element name="manufacturer" type="xsd:string"/>
        <xsd:element name="ean" type="xsd:string"/>
        <xsd:element name="model" type="xsd:string"/>
        <xsd:element name="item_category_id" type="xsd:int"/>
        <xsd:element name="cost_centre_id" type="xsd:int"/>
        <xsd:element name="invoice_nr" type="xsd:string"/>
        <xsd:element name="provider_id" type="xsd:int"/>
        <xsd:element name="purchase_date" type="xsd:string"/>
        <xsd:element name="amortization_month" type="xsd:int"/>
        <xsd:element name="original_warehouse_id" type="xsd:int"/>
        <xsd:element name="original_warehouse_area_id" type="xsd:int"/>
        <xsd:element name="label_number" type="xsd:int"/>
        <xsd:element name="insurance_company" type="xsd:string"/>
        <xsd:element name="insurance_policy" type="xsd:string"/>
        <xsd:element name="auto_insurance" type="xsd:int"/>
        <xsd:element name="sensor_matic" type="xsd:int"/>
        <xsd:element name="rental_cost" type="xsd:decimal"/>
        <xsd:element name="rental_payment_mode" type="xsd:int"/>
        <xsd:element name="lease_cost" type="xsd:decimal"/>
        <xsd:element name="lease_payment_mode" type="xsd:int"/>
        <xsd:element name="lease_date" type="xsd:string"/>
        <xsd:element name="lease_residual_value" type="xsd:decimal"/>
        <xsd:element name="rental_date" type="xsd:string"/>
        <xsd:element name="condition" type="xsd:int"/>
        <xsd:element name="asset_ext_id" type="xsd:string"/>
        <xsd:element name="last_asset_review_date" type="xsd:string"/>
        */

        assetSoapObject.addProperty("asset_id", asset.asset_id)
        assetSoapObject.addProperty("parent_id", asset.parent_id)
        assetSoapObject.addProperty("code", asset.code)
        assetSoapObject.addProperty("price", asset.price)
        assetSoapObject.addProperty("warehouse_id", asset.warehouse_id)
        assetSoapObject.addProperty("warehouse_area_id", asset.warehouse_area_id)
        assetSoapObject.addProperty("active", asset.active)
        assetSoapObject.addProperty("reason", asset.reason)
        assetSoapObject.addProperty("serial_number", asset.serial_number)
        assetSoapObject.addProperty("obs", asset.obs)
        assetSoapObject.addProperty("ownership_status", asset.ownership_status)
        assetSoapObject.addProperty("warranty_due", asset.warranty_due)
        assetSoapObject.addProperty("status", asset.status)
        assetSoapObject.addProperty("missing_date", asset.missing_date)
        assetSoapObject.addProperty("removed_date", asset.removed_date)
        assetSoapObject.addProperty("description", asset.description)
        assetSoapObject.addProperty("manufacturer", asset.manufacturer)
        assetSoapObject.addProperty("ean", asset.ean)
        assetSoapObject.addProperty("model", asset.model)
        assetSoapObject.addProperty("item_category_id", asset.item_category_id)
        assetSoapObject.addProperty("cost_centre_id", asset.cost_centre_id)
        assetSoapObject.addProperty("invoice_nr", asset.invoice_nr)
        assetSoapObject.addProperty("provider_id", asset.provider_id)
        assetSoapObject.addProperty("purchase_date", asset.purchase_date)
        assetSoapObject.addProperty("amortization_month", asset.amortization_month)
        assetSoapObject.addProperty("original_warehouse_id", asset.original_warehouse_id)
        assetSoapObject.addProperty("original_warehouse_area_id", asset.original_warehouse_area_id)
        assetSoapObject.addProperty("label_number", asset.label_number)
        assetSoapObject.addProperty("insurance_company", asset.insurance_company)
        assetSoapObject.addProperty("insurance_policy", asset.insurance_policy)
        assetSoapObject.addProperty("auto_insurance", asset.auto_insurance)
        assetSoapObject.addProperty("sensor_matic", asset.sensor_matic)
        assetSoapObject.addProperty("rental_cost", asset.rental_cost)
        assetSoapObject.addProperty("rental_payment_mode", asset.rental_payment_mode)
        assetSoapObject.addProperty("lease_cost", asset.lease_cost)
        assetSoapObject.addProperty("lease_payment_mode", asset.lease_payment_mode)
        assetSoapObject.addProperty("lease_date", asset.lease_date)
        assetSoapObject.addProperty("lease_residual_value", asset.lease_residual_value)
        assetSoapObject.addProperty("rental_date", asset.rental_date)
        assetSoapObject.addProperty("condition", asset.condition)
        assetSoapObject.addProperty("asset_ext_id", asset.asset_ext_id)
        assetSoapObject.addProperty("last_asset_review_date", asset.last_asset_review_date)

        val result = getWebservice().s(
            "Asset_Add",
            arrayOf(WsParam("user_id", userId)),
            assetSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun assetCollectorAdd(
        userId: Long,
        asset: AssetCollectorObject,
    ): Long {
        val assetSoapObject = SoapObject(getWebservice().namespace, "asset")
        assetSoapObject.addProperty("asset_id", asset.asset_id)
        assetSoapObject.addProperty("parent_id", asset.parent_id)
        assetSoapObject.addProperty("code", asset.code)
        assetSoapObject.addProperty("warehouse_id", asset.warehouse_id)
        assetSoapObject.addProperty("warehouse_area_id", asset.warehouse_area_id)
        assetSoapObject.addProperty("active", asset.active)
        assetSoapObject.addProperty("serial_number2", asset.serial_number)
        assetSoapObject.addProperty("ownership_status", asset.ownership_status)
        assetSoapObject.addProperty("status", asset.status)
        assetSoapObject.addProperty("missing_date", asset.missing_date)
        assetSoapObject.addProperty("description", asset.description)
        assetSoapObject.addProperty("ean", asset.ean)
        assetSoapObject.addProperty("item_category_id", asset.item_category_id)
        assetSoapObject.addProperty("original_warehouse_id", asset.original_warehouse_id)
        assetSoapObject.addProperty("original_warehouse_area_id", asset.original_warehouse_area_id)
        assetSoapObject.addProperty("label_number", asset.label_number)
        assetSoapObject.addProperty("condition", asset.condition)
        assetSoapObject.addProperty("last_asset_review_date", asset.last_asset_review_date)

        val result = getWebservice().s(
            "Asset_Collector_Add",
            arrayOf(WsParam("user_id", userId)),
            assetSoapObject
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun assetCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "Asset_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}