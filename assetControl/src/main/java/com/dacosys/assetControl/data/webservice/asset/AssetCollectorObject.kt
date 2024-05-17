package com.dacosys.assetControl.data.webservice.asset

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.data.room.entity.asset.Asset
import org.ksoap2.serialization.SoapObject

class AssetCollectorObject() : Parcelable {
    var asset_id = 0L
    var parent_id = 0L
    var code = String()
    var warehouse_id = 0L
    var warehouse_area_id = 0L
    var active = 0
    var ownership_status = 0
    var status = 0
    var missing_date = String()
    var description = String()
    var item_category_id = 0L
    var original_warehouse_id = 0L
    var original_warehouse_area_id = 0L
    var label_number = 0
    var condition = 0
    var serial_number = String()
    var ean = String()
    var last_asset_review_date = String()

    constructor(parcel: Parcel) : this() {
        asset_id = parcel.readLong()
        parent_id = parcel.readLong()
        code = parcel.readString().orEmpty()
        warehouse_id = parcel.readLong()
        warehouse_area_id = parcel.readLong()
        active = parcel.readInt()
        ownership_status = parcel.readInt()
        status = parcel.readInt()
        missing_date = parcel.readString().orEmpty()
        description = parcel.readString().orEmpty()
        item_category_id = parcel.readLong()
        original_warehouse_id = parcel.readLong()
        original_warehouse_area_id = parcel.readLong()
        label_number = parcel.readInt()
        condition = parcel.readInt()
        serial_number = parcel.readString().orEmpty()
        ean = parcel.readString().orEmpty()
        last_asset_review_date = parcel.readString().orEmpty()
    }

    constructor(asset: Asset) : this() {
        asset_id = asset.id
        description = asset.description
        code = asset.code
        item_category_id = asset.itemCategoryId
        warehouse_area_id = asset.warehouseAreaId
        warehouse_id = asset.warehouseId
        original_warehouse_area_id = asset.originalWarehouseAreaId
        original_warehouse_id = asset.originalWarehouseId
        status = asset.status
        ownership_status = asset.ownershipStatus
        active = asset.active
        missing_date = asset.missingDate ?: ""
        label_number = asset.labelNumber ?: 0
        condition = asset.condition ?: 0
        parent_id = asset.parentId ?: 0
        ean = asset.ean ?: ""
        serial_number = asset.serialNumber ?: ""
        last_asset_review_date = asset.lastAssetReviewDate ?: ""
    }

    fun getBySoapObject(so: SoapObject): AssetCollectorObject {
        val x = AssetCollectorObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "asset_id" -> {
                            x.asset_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "parent_id" -> {
                            x.parent_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "code" -> {
                            x.code = soValue as? String ?: ""
                        }

                        "warehouse_id" -> {
                            x.warehouse_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "warehouse_area_id" -> {
                            x.warehouse_area_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "active" -> {
                            x.active = soValue as? Int ?: 0
                        }

                        "ownership_status" -> {
                            x.ownership_status = soValue as? Int ?: 0
                        }

                        "status" -> {
                            x.status = soValue as? Int ?: 0
                        }

                        "missing_date" -> {
                            x.missing_date = soValue as? String ?: ""
                        }

                        "description" -> {
                            x.description = soValue as? String ?: ""
                        }

                        "item_category_id" -> {
                            x.item_category_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "original_warehouse_id" -> {
                            x.original_warehouse_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "original_warehouse_area_id" -> {
                            x.original_warehouse_area_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "label_number" -> {
                            x.label_number = soValue as? Int ?: 0
                        }

                        "condition" -> {
                            x.condition = soValue as? Int ?: 0
                        }

                        "serial_number" -> {
                            x.serial_number = soValue as? String ?: ""
                        }

                        "ean" -> {
                            x.ean = soValue as? String ?: ""
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
        parcel.writeLong(warehouse_id)
        parcel.writeLong(warehouse_area_id)
        parcel.writeInt(active)
        parcel.writeInt(ownership_status)
        parcel.writeInt(status)
        parcel.writeString(missing_date)
        parcel.writeString(description)
        parcel.writeLong(item_category_id)
        parcel.writeLong(original_warehouse_id)
        parcel.writeLong(original_warehouse_area_id)
        parcel.writeInt(label_number)
        parcel.writeInt(condition)
        parcel.writeString(serial_number)
        parcel.writeString(ean)
        parcel.writeString(last_asset_review_date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetCollectorObject> {
        override fun createFromParcel(parcel: Parcel): AssetCollectorObject {
            return AssetCollectorObject(parcel)
        }

        override fun newArray(size: Int): Array<AssetCollectorObject?> {
            return arrayOfNulls(size)
        }
    }
}


