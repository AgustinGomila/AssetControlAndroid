package com.example.assetControl.data.webservice.asset

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.data.room.dto.asset.Asset
import org.ksoap2.serialization.SoapObject


class AssetObject() : Parcelable {
    var asset_id = 0L
    var parent_id = 0L
    var code = String()
    var price = 0.0
    var warehouse_id = 0L
    var warehouse_area_id = 0L
    var active = 0
    var reason = String()
    var serial_number = String()
    var obs = String()
    var ownership_status = 0
    var warranty_due = String()
    var status = 0
    var missing_date = String()
    var removed_date = String()
    var description = String()
    var manufacturer = String()
    var ean = String()
    var model = String()
    var item_category_id = 0L
    var cost_centre_id = 0L
    var invoice_nr = String()
    var provider_id = 0L
    var purchase_date = String()
    var amortization_month = 0
    var original_warehouse_id = 0L
    var original_warehouse_area_id = 0L
    var label_number = 0
    var insurance_company = String()
    var insurance_policy = String()
    var auto_insurance = 0
    var sensor_matic = 0
    var rental_cost = 0.0
    var rental_payment_mode = 0
    var lease_cost = 0.0
    var lease_payment_mode = 0
    var lease_date = String()
    var lease_residual_value = 0.0
    var rental_date = String()
    var condition = 0
    var asset_ext_id = String()
    var last_asset_review_date = String()

    constructor(parcel: Parcel) : this() {
        asset_id = parcel.readLong()
        parent_id = parcel.readLong()
        code = parcel.readString().orEmpty()
        price = parcel.readDouble()
        warehouse_id = parcel.readLong()
        warehouse_area_id = parcel.readLong()
        active = parcel.readInt()
        reason = parcel.readString().orEmpty()
        serial_number = parcel.readString().orEmpty()
        obs = parcel.readString().orEmpty()
        ownership_status = parcel.readInt()
        warranty_due = parcel.readString().orEmpty()
        status = parcel.readInt()
        missing_date = parcel.readString().orEmpty()
        removed_date = parcel.readString().orEmpty()
        description = parcel.readString().orEmpty()
        manufacturer = parcel.readString().orEmpty()
        ean = parcel.readString().orEmpty()
        model = parcel.readString().orEmpty()
        item_category_id = parcel.readLong()
        cost_centre_id = parcel.readLong()
        invoice_nr = parcel.readString().orEmpty()
        provider_id = parcel.readLong()
        purchase_date = parcel.readString().orEmpty()
        amortization_month = parcel.readInt()
        original_warehouse_id = parcel.readLong()
        original_warehouse_area_id = parcel.readLong()
        label_number = parcel.readInt()
        insurance_company = parcel.readString().orEmpty()
        insurance_policy = parcel.readString().orEmpty()
        auto_insurance = parcel.readInt()
        sensor_matic = parcel.readInt()
        rental_cost = parcel.readDouble()
        rental_payment_mode = parcel.readInt()
        lease_cost = parcel.readDouble()
        lease_payment_mode = parcel.readInt()
        lease_date = parcel.readString().orEmpty()
        lease_residual_value = parcel.readDouble()
        rental_date = parcel.readString().orEmpty()
        condition = parcel.readInt()
        asset_ext_id = parcel.readString().orEmpty()
        last_asset_review_date = parcel.readString().orEmpty()
    }

    constructor(asset: Asset) : this() {
        // Main Information
        description = asset.description
        code = asset.code
        item_category_id = asset.itemCategoryId

        cost_centre_id = 0L
        parent_id = 0L

        warehouse_area_id = asset.warehouseAreaId
        warehouse_id = asset.warehouseId

        original_warehouse_area_id = asset.originalWarehouseAreaId
        original_warehouse_id = asset.originalWarehouseId

        status = asset.status
        ownership_status = asset.ownershipStatus
        active = asset.active

        // Secondary Information
        manufacturer = asset.manufacturer ?: ""
        model = asset.model ?: ""
        serial_number = asset.serialNumber ?: ""
        condition = asset.condition ?: 0
        obs = ""
        provider_id = 0L
        ean = asset.ean ?: ""

        // Insurance Information
        insurance_company = ""
        insurance_policy = ""
        auto_insurance = 0
        sensor_matic = 0

        // Removed
        removed_date = ""
        reason = ""

        // Missed
        missing_date = asset.missingDate ?: ""

        // Purchased
        price = 0.0
        purchase_date = ""
        invoice_nr = ""
        warranty_due = ""
        amortization_month = 0

        // Rented
        rental_cost = 0.0
        rental_payment_mode = 0
        rental_date = ""

        // Leased
        lease_cost = 0.0
        lease_payment_mode = 0
        lease_date = ""
        lease_residual_value = 0.0
        amortization_month = 0
        last_asset_review_date = asset.lastAssetReviewDate ?: ""
    }

    fun getBySoapObject(so: SoapObject): AssetObject {
        val x = AssetObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "asset_id" -> {
                            x.asset_id =
                                (soValue as? Int)?.toLong() ?: (soValue as? Long ?: 0L)
                        }

                        "parent_id" -> {
                            x.parent_id =
                                (soValue as? Int)?.toLong() ?: (soValue as? Long ?: 0L)
                        }

                        "code" -> {
                            x.code = soValue as? String ?: ""
                        }

                        "price" -> {
                            x.price = soValue as? Double ?: 0.0
                        }

                        "warehouse_id" -> {
                            x.warehouse_id =
                                (soValue as? Int)?.toLong() ?: (soValue as? Long ?: 0L)
                        }

                        "warehouse_area_id" -> {
                            x.warehouse_area_id =
                                (soValue as? Int)?.toLong() ?: (soValue as? Long ?: 0L)
                        }

                        "active" -> {
                            x.active = soValue as? Int ?: 0
                        }

                        "reason" -> {
                            x.reason = soValue as? String ?: ""
                        }

                        "serial_number" -> {
                            x.serial_number = soValue as? String ?: ""
                        }

                        "obs" -> {
                            x.obs = soValue as? String ?: ""
                        }

                        "ownership_status" -> {
                            x.ownership_status = soValue as? Int ?: 0
                        }

                        "warranty_due" -> {
                            x.warranty_due = soValue as? String ?: ""
                        }

                        "status" -> {
                            x.status = soValue as? Int ?: 0
                        }

                        "missing_date" -> {
                            x.missing_date = soValue as? String ?: ""
                        }

                        "removed_date" -> {
                            x.removed_date = soValue as? String ?: ""
                        }

                        "description" -> {
                            x.description = soValue as? String ?: ""
                        }

                        "manufacturer" -> {
                            x.manufacturer = soValue as? String ?: ""
                        }

                        "ean" -> {
                            x.ean = soValue as? String ?: ""
                        }

                        "model" -> {
                            x.model = soValue as? String ?: ""
                        }

                        "item_category_id" -> {
                            x.item_category_id =
                                (soValue as? Int)?.toLong() ?: (soValue as? Long ?: 0L)
                        }

                        "cost_centre_id" -> {
                            x.cost_centre_id =
                                (soValue as? Int)?.toLong() ?: (soValue as? Long ?: 0L)
                        }

                        "invoice_nr" -> {
                            x.invoice_nr = soValue as? String ?: ""
                        }

                        "provider_id" -> {
                            x.provider_id =
                                (soValue as? Int)?.toLong() ?: (soValue as? Long ?: 0L)
                        }

                        "purchase_date" -> {
                            x.purchase_date = soValue as? String ?: ""
                        }

                        "amortization_month" -> {
                            x.amortization_month = soValue as? Int ?: 0
                        }

                        "original_warehouse_id" -> {
                            x.original_warehouse_id =
                                (soValue as? Int)?.toLong() ?: (soValue as? Long ?: 0L)
                        }

                        "original_warehouse_area_id" -> {
                            x.original_warehouse_area_id =
                                (soValue as? Int)?.toLong() ?: (soValue as? Long ?: 0L)
                        }

                        "label_number" -> {
                            x.label_number = soValue as? Int ?: 0
                        }

                        "insurance_company" -> {
                            x.insurance_company = soValue as? String ?: ""
                        }

                        "insurance_policy" -> {
                            x.insurance_policy = soValue as? String ?: ""
                        }

                        "auto_insurance" -> {
                            x.auto_insurance = soValue as? Int ?: 0
                        }

                        "sensor_matic" -> {
                            x.sensor_matic = soValue as? Int ?: 0
                        }

                        "rental_cost" -> {
                            x.rental_cost = soValue as? Double ?: 0.0
                        }

                        "rental_payment_mode" -> {
                            x.rental_payment_mode = soValue as? Int ?: 0
                        }

                        "lease_cost =  " -> {
                            x.lease_cost = soValue as? Double ?: 0.0
                        }

                        "lease_payment_mode" -> {
                            x.lease_payment_mode = soValue as? Int ?: 0
                        }

                        "lease_date" -> {
                            x.lease_date = soValue as? String ?: ""
                        }

                        "lease_residual_value" -> {
                            x.lease_residual_value = soValue as? Double ?: 0.0
                        }

                        "rental_date" -> {
                            x.rental_date = soValue as? String ?: ""
                        }

                        "condition" -> {
                            x.condition = soValue as? Int ?: 0
                        }

                        "asset_ext_id" -> {
                            x.asset_ext_id = soValue as? String ?: ""
                        }

                        "last_asset_review_date" -> {
                            x.last_asset_review_date = soValue as? String ?: ""
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(asset_id)
        parcel.writeLong(parent_id)
        parcel.writeString(code)
        parcel.writeDouble(price)
        parcel.writeLong(warehouse_id)
        parcel.writeLong(warehouse_area_id)
        parcel.writeInt(active)
        parcel.writeString(reason)
        parcel.writeString(serial_number)
        parcel.writeString(obs)
        parcel.writeInt(ownership_status)
        parcel.writeString(warranty_due)
        parcel.writeInt(status)
        parcel.writeString(missing_date)
        parcel.writeString(removed_date)
        parcel.writeString(description)
        parcel.writeString(manufacturer)
        parcel.writeString(ean)
        parcel.writeString(model)
        parcel.writeLong(item_category_id)
        parcel.writeLong(cost_centre_id)
        parcel.writeString(invoice_nr)
        parcel.writeLong(provider_id)
        parcel.writeString(purchase_date)
        parcel.writeInt(amortization_month)
        parcel.writeLong(original_warehouse_id)
        parcel.writeLong(original_warehouse_area_id)
        parcel.writeInt(label_number)
        parcel.writeString(insurance_company)
        parcel.writeString(insurance_policy)
        parcel.writeInt(auto_insurance)
        parcel.writeInt(sensor_matic)
        parcel.writeDouble(rental_cost)
        parcel.writeInt(rental_payment_mode)
        parcel.writeDouble(lease_cost)
        parcel.writeInt(lease_payment_mode)
        parcel.writeString(lease_date)
        parcel.writeDouble(lease_residual_value)
        parcel.writeString(rental_date)
        parcel.writeInt(condition)
        parcel.writeString(asset_ext_id)
        parcel.writeString(last_asset_review_date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetObject> {
        override fun createFromParcel(parcel: Parcel): AssetObject {
            return AssetObject(parcel)
        }

        override fun newArray(size: Int): Array<AssetObject?> {
            return arrayOfNulls(size)
        }
    }
}


