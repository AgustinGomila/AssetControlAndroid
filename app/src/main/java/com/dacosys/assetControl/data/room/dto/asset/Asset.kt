package com.dacosys.assetControl.data.room.dto.asset

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.enums.asset.AssetCondition
import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.enums.asset.OwnershipStatus
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.webservice.asset.AssetCollectorObject
import com.dacosys.assetControl.data.webservice.asset.AssetObject

abstract class AssetEntry {
    companion object {
        const val TABLE_NAME = "asset"
        const val ID = "_id"
        const val CODE = "code"
        const val DESCRIPTION = "description"
        const val WAREHOUSE_ID = "warehouse_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val ACTIVE = "active"
        const val OWNERSHIP_STATUS = "ownership_status"
        const val STATUS = "status"
        const val MISSING_DATE = "missing_date"
        const val ITEM_CATEGORY_ID = "item_category_id"
        const val TRANSFERRED = "transferred"
        const val ORIGINAL_WAREHOUSE_ID = "original_warehouse_id"
        const val ORIGINAL_WAREHOUSE_AREA_ID = "original_warehouse_area_id"
        const val LABEL_NUMBER = "label_number"
        const val MANUFACTURER = "manufacturer"
        const val MODEL = "model"
        const val SERIAL_NUMBER = "serial_number"
        const val CONDITION = "condition"
        const val COST_CENTRE_ID = "cost_centre_id"
        const val PARENT_ID = "parent_id"
        const val EAN = "ean"
        const val LAST_ASSET_REVIEW_DATE = "last_asset_review_date"

        const val ITEM_CATEGORY_STR = "item_category_str"
        const val WAREHOUSE_STR = "warehouse_str"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        const val ORIGINAL_WAREHOUSE_STR = "orig_warehouse_str"
        const val ORIGINAL_WAREHOUSE_AREA_STR = "orig_warehouse_area_str"
    }
}

class Asset(
    @ColumnInfo(name = AssetEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = AssetEntry.CODE) var code: String = "",
    @ColumnInfo(name = AssetEntry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = AssetEntry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = AssetEntry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long = 0L,
    @ColumnInfo(name = AssetEntry.ACTIVE, defaultValue = "1") var active: Int = 1,
    @ColumnInfo(name = AssetEntry.OWNERSHIP_STATUS, defaultValue = "1") var ownershipStatus: Int = 1,
    @ColumnInfo(name = AssetEntry.STATUS, defaultValue = "1") var status: Int = 1,
    @ColumnInfo(name = AssetEntry.MISSING_DATE) var missingDate: String? = null,
    @ColumnInfo(name = AssetEntry.ITEM_CATEGORY_ID, defaultValue = "0") var itemCategoryId: Long = 0L,
    @ColumnInfo(name = AssetEntry.TRANSFERRED) var transferred: Int? = null,
    @ColumnInfo(name = AssetEntry.ORIGINAL_WAREHOUSE_ID) var originalWarehouseId: Long = 0L,
    @ColumnInfo(name = AssetEntry.ORIGINAL_WAREHOUSE_AREA_ID) var originalWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = AssetEntry.LABEL_NUMBER) var labelNumber: Int? = null,
    @ColumnInfo(name = AssetEntry.MANUFACTURER) var manufacturer: String? = null,
    @ColumnInfo(name = AssetEntry.MODEL) var model: String? = null,
    @ColumnInfo(name = AssetEntry.SERIAL_NUMBER) var serialNumber: String? = null,
    @ColumnInfo(name = AssetEntry.CONDITION) var condition: Int? = null,
    @ColumnInfo(name = AssetEntry.COST_CENTRE_ID) var costCentreId: Long? = null,
    @ColumnInfo(name = AssetEntry.PARENT_ID) var parentId: Long? = null,
    @ColumnInfo(name = AssetEntry.EAN) var ean: String? = null,
    @ColumnInfo(name = AssetEntry.LAST_ASSET_REVIEW_DATE) var lastAssetReviewDate: String? = null,
    @ColumnInfo(name = AssetEntry.ITEM_CATEGORY_STR) var itemCategoryDescription: String? = null,
    @ColumnInfo(name = AssetEntry.WAREHOUSE_STR) var warehouseDescription: String? = null,
    @ColumnInfo(name = AssetEntry.WAREHOUSE_AREA_STR) var warehouseAreaDescription: String? = null,
    @ColumnInfo(name = AssetEntry.ORIGINAL_WAREHOUSE_STR) var originalWarehouseDescription: String? = null,
    @ColumnInfo(name = AssetEntry.ORIGINAL_WAREHOUSE_AREA_STR) var originalWarehouseAreaDescription: String? = null,
) : Parcelable {

    override fun toString(): String {
        return code
    }

    fun saveChanges() = AssetRepository().update(this)

    @Ignore
    var itemCategoryStr = itemCategoryDescription.orEmpty()
        get() = itemCategoryDescription.orEmpty()
        set(value) {
            itemCategoryDescription = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var warehouseStr = warehouseDescription.orEmpty()
        get() = warehouseDescription.orEmpty()
        set(value) {
            warehouseDescription = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var warehouseAreaStr = warehouseAreaDescription.orEmpty()
        get() = warehouseAreaDescription.orEmpty()
        set(value) {
            warehouseAreaDescription = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var originalWarehouseStr = originalWarehouseDescription.orEmpty()
        get() = originalWarehouseDescription.orEmpty()
        set(value) {
            originalWarehouseDescription = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var originalWarehouseAreaStr = originalWarehouseAreaDescription.orEmpty()
        get() = originalWarehouseAreaDescription.orEmpty()
        set(value) {
            originalWarehouseAreaDescription = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var assetStatus: AssetStatus = AssetStatus.getById(status)
        get() = AssetStatus.getById(status)
        set(value) {
            status = value.id
            field = value
        }

    @Ignore
    var ownership = OwnershipStatus.getById(ownershipStatus)
        get() = OwnershipStatus.getById(ownershipStatus)
        set(value) {
            ownershipStatus = value.id
            field = value
        }

    @Ignore
    var assetCondition: AssetCondition = getCondition()
        get() = getCondition()
        set(value) {
            condition = value.id
            field = value
        }

    private fun getCondition(): AssetCondition {
        val c = condition
        return if (c == null) AssetCondition.unknown
        else AssetCondition.getById(c)
    }

    constructor() : this(
        id = 0L,
        code = "",
        description = "",
        warehouseId = 0L,
        warehouseAreaId = 0L,
        active = 1,
        ownershipStatus = 1,
        status = 1,
        missingDate = null,
        itemCategoryId = 0L,
        transferred = null,
        originalWarehouseId = 0L,
        originalWarehouseAreaId = 0L,
        labelNumber = null,
        manufacturer = null,
        model = null,
        serialNumber = null,
        condition = null,
        costCentreId = null,
        parentId = null,
        ean = null,
        lastAssetReviewDate = null,
        itemCategoryDescription = "",
        warehouseDescription = "",
        warehouseAreaDescription = "",
        originalWarehouseDescription = "",
        originalWarehouseAreaDescription = "",
    )

    constructor(a: AssetObject) : this(
        id = a.asset_id,
        code = a.code,
        description = a.description,
        warehouseId = a.warehouse_id,
        warehouseAreaId = a.warehouse_area_id,
        active = a.active,
        ownershipStatus = a.ownership_status,
        status = a.status,
        missingDate = a.missing_date,
        itemCategoryId = a.item_category_id,
        transferred = 1,
        originalWarehouseId = a.original_warehouse_id,
        originalWarehouseAreaId = a.original_warehouse_area_id,
        labelNumber = a.label_number,
        manufacturer = a.manufacturer,
        model = a.model,
        serialNumber = a.serial_number,
        condition = a.condition,
        costCentreId = a.cost_centre_id,
        parentId = a.parent_id,
        ean = a.ean,
        lastAssetReviewDate = a.last_asset_review_date,
        itemCategoryDescription = "",
        warehouseDescription = "",
        warehouseAreaDescription = "",
        originalWarehouseDescription = "",
        originalWarehouseAreaDescription = "",
    )

    constructor(aco: AssetCollectorObject) : this(
        id = aco.asset_id,
        code = aco.code,
        description = aco.description,
        warehouseId = aco.warehouse_id,
        warehouseAreaId = aco.warehouse_area_id,
        active = aco.active,
        ownershipStatus = aco.ownership_status,
        status = aco.status,
        missingDate = aco.missing_date,
        itemCategoryId = aco.item_category_id,
        transferred = 1,
        originalWarehouseId = aco.original_warehouse_id,
        originalWarehouseAreaId = aco.original_warehouse_area_id,
        labelNumber = aco.label_number,
        manufacturer = "",
        model = "",
        serialNumber = aco.serial_number,
        condition = aco.condition,
        costCentreId = 0, // aco.cost_centre_id,
        parentId = aco.parent_id,
        ean = aco.ean,
        lastAssetReviewDate = aco.last_asset_review_date,
        itemCategoryDescription = "",
        warehouseDescription = "",
        warehouseAreaDescription = "",
        originalWarehouseDescription = "",
        originalWarehouseAreaDescription = "",
    )

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        code = parcel.readString().orEmpty(),
        description = parcel.readString().orEmpty(),
        warehouseId = parcel.readLong(),
        warehouseAreaId = parcel.readLong(),
        active = parcel.readInt(),
        ownershipStatus = parcel.readInt(),
        status = parcel.readInt(),
        missingDate = parcel.readString(),
        itemCategoryId = parcel.readLong(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        originalWarehouseId = parcel.readLong(),
        originalWarehouseAreaId = parcel.readLong(),
        labelNumber = parcel.readValue(Int::class.java.classLoader) as? Int,
        manufacturer = parcel.readString(),
        model = parcel.readString(),
        serialNumber = parcel.readString(),
        condition = parcel.readValue(Int::class.java.classLoader) as? Int,
        costCentreId = parcel.readValue(Long::class.java.classLoader) as? Long,
        parentId = parcel.readValue(Long::class.java.classLoader) as? Long,
        ean = parcel.readString(),
        lastAssetReviewDate = parcel.readString(),
        itemCategoryDescription = parcel.readString().orEmpty(),
        warehouseDescription = parcel.readString().orEmpty(),
        warehouseAreaDescription = parcel.readString().orEmpty(),
        originalWarehouseDescription = parcel.readString().orEmpty(),
        originalWarehouseAreaDescription = parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(code)
        parcel.writeString(description)
        parcel.writeLong(warehouseId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeInt(active)
        parcel.writeInt(ownershipStatus)
        parcel.writeInt(status)
        parcel.writeString(missingDate)
        parcel.writeLong(itemCategoryId)
        parcel.writeValue(transferred)
        parcel.writeLong(originalWarehouseId)
        parcel.writeLong(originalWarehouseAreaId)
        parcel.writeValue(labelNumber)
        parcel.writeString(manufacturer)
        parcel.writeString(model)
        parcel.writeString(serialNumber)
        parcel.writeValue(condition)
        parcel.writeValue(costCentreId)
        parcel.writeValue(parentId)
        parcel.writeString(ean)
        parcel.writeString(lastAssetReviewDate)
        parcel.writeString(itemCategoryDescription)
        parcel.writeString(warehouseDescription)
        parcel.writeString(warehouseAreaDescription)
        parcel.writeString(originalWarehouseDescription)
        parcel.writeString(originalWarehouseAreaDescription)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Asset

        return id == other.id
    }

    companion object CREATOR : Parcelable.Creator<Asset> {
        override fun createFromParcel(parcel: Parcel): Asset {
            return Asset(parcel)
        }

        override fun newArray(size: Int): Array<Asset?> {
            return arrayOfNulls(size)
        }
    }
}