package com.example.assetControl.data.room.dto.movement

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.dto.review.AssetReviewContent
import com.example.assetControl.data.room.entity.movement.TempMovementContentEntity
import com.example.assetControl.data.room.repository.asset.AssetRepository

abstract class WarehouseMovementContentEntry {
    companion object {
        const val TABLE_NAME = "warehouse_movement_content"
        const val ID = "_id"
        const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
        const val ASSET_ID = "asset_id"
        const val CODE = "code"
        const val QTY = "qty"

        const val CONTENT_STATUS_ID = "content_status_id"
        const val DESCRIPTION = "description"
        const val ASSET_STATUS_ID = "asset_status_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val LABEL_NUMBER = "label_number"
        const val PARENT_ID = "parent_id"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        const val WAREHOUSE_STR = "warehouse_str"
        const val ITEM_CATEGORY_ID = "item_category_id"
        const val ITEM_CATEGORY_STR = "item_category_str"
        const val OWNERSHIP_STATUS_ID = "ownership_status_id"
        const val MANUFACTURER = "manufacturer"
        const val MODEL = "model"
        const val SERIAL_NUMBER = "serial_number"
        const val EAN = "ean"
    }
}

data class WarehouseMovementContent(
    @ColumnInfo(name = WarehouseMovementContentEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID) var warehouseMovementId: Long = 0L,
    @ColumnInfo(name = WarehouseMovementContentEntry.ASSET_ID) var assetId: Long = 0L,
    @ColumnInfo(name = WarehouseMovementContentEntry.CODE) var code: String = "",
    @ColumnInfo(name = WarehouseMovementContentEntry.QTY) var qty: Double? = null,
    @ColumnInfo(name = WarehouseMovementContentEntry.CONTENT_STATUS_ID) var contentStatusId: Int = 0,
    @ColumnInfo(name = WarehouseMovementContentEntry.ASSET_STATUS_ID) var assetStatusId: Int = 0,
    @ColumnInfo(name = WarehouseMovementContentEntry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = WarehouseMovementContentEntry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long = 0L,
    @ColumnInfo(name = WarehouseMovementContentEntry.LABEL_NUMBER) var labelNumber: Int = 0,
    @ColumnInfo(name = WarehouseMovementContentEntry.PARENT_ID) var parentId: Long? = null,
    @ColumnInfo(name = WarehouseMovementContentEntry.WAREHOUSE_AREA_STR) var warehouseAreaDescription: String? = null,
    @ColumnInfo(name = WarehouseMovementContentEntry.WAREHOUSE_STR) var warehouseDescription: String? = null,
    @ColumnInfo(name = WarehouseMovementContentEntry.ITEM_CATEGORY_ID) var itemCategoryId: Long = 0L,
    @ColumnInfo(name = WarehouseMovementContentEntry.ITEM_CATEGORY_STR) var itemCategoryDescription: String? = null,
    @ColumnInfo(name = WarehouseMovementContentEntry.OWNERSHIP_STATUS_ID) var ownershipStatusId: Int = 0,
    @ColumnInfo(name = WarehouseMovementContentEntry.MANUFACTURER) var manufacturer: String? = null,
    @ColumnInfo(name = WarehouseMovementContentEntry.MODEL) var model: String? = null,
    @ColumnInfo(name = WarehouseMovementContentEntry.SERIAL_NUMBER) var serialNumber: String? = null,
    @ColumnInfo(name = WarehouseMovementContentEntry.EAN) var ean: String? = null,
) : Parcelable {

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WarehouseMovementContent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    @Ignore
    private var assetRead: Boolean = false

    @Ignore
    private lateinit var mAsset: Asset

    fun asset(): Asset {
        if (!assetRead) {
            mAsset = AssetRepository().selectById(assetId) ?: Asset()
        }
        return mAsset
    }

    constructor(movementId: Long, id: Long, asset: Asset, contentStatusId: Int) : this(
        warehouseMovementId = movementId,
        id = id,
        assetId = asset.id,
        code = asset.code,
        qty = 1.0,
        contentStatusId = contentStatusId,
        assetStatusId = asset.status,
        description = asset.description,
        warehouseAreaId = asset.warehouseAreaId,
        labelNumber = asset.labelNumber ?: 0,
        parentId = asset.parentId,
        warehouseAreaDescription = asset.warehouseAreaStr,
        warehouseDescription = asset.warehouseStr,
        itemCategoryId = asset.itemCategoryId,
        itemCategoryDescription = asset.itemCategoryStr,
        ownershipStatusId = asset.ownershipStatus,
        manufacturer = asset.manufacturer.orEmpty(),
        model = asset.model.orEmpty(),
        serialNumber = asset.serialNumber.orEmpty(),
        ean = asset.ean.orEmpty()
    )

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        warehouseMovementId = parcel.readLong(),
        assetId = parcel.readLong(),
        code = parcel.readString().orEmpty(),
        qty = parcel.readValue(Double::class.java.classLoader) as? Double,
        contentStatusId = parcel.readInt(),
        assetStatusId = parcel.readInt(),
        description = parcel.readString().orEmpty(),
        warehouseAreaId = parcel.readLong(),
        labelNumber = parcel.readInt(),
        parentId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseAreaDescription = parcel.readString().orEmpty(),
        warehouseDescription = parcel.readString().orEmpty(),
        itemCategoryId = parcel.readLong(),
        itemCategoryDescription = parcel.readString().orEmpty(),
        ownershipStatusId = parcel.readInt(),
        manufacturer = parcel.readString().orEmpty(),
        model = parcel.readString().orEmpty(),
        serialNumber = parcel.readString().orEmpty(),
        ean = parcel.readString().orEmpty()
    )

    constructor(id: Long, movementId: Long, reviewContent: AssetReviewContent) : this(
        id = id,
        warehouseMovementId = movementId,
        assetId = reviewContent.assetId,
        code = reviewContent.code,
        qty = reviewContent.qty
    )

    constructor(tContent: TempMovementContentEntity) : this(
        id = tContent.id,
        warehouseMovementId = tContent.warehouseMovementId,
        assetId = tContent.assetId,
        code = tContent.code,
        qty = tContent.qty,
        contentStatusId = tContent.contentStatus,
        assetStatusId = tContent.status,
        description = tContent.description,
        warehouseAreaId = tContent.warehouseAreaId,
        labelNumber = tContent.labelNumber ?: 0,
        parentId = tContent.parentId,
        warehouseDescription = tContent.warehouseStr,
        warehouseAreaDescription = tContent.warehouseAreaStr,
        itemCategoryId = tContent.itemCategoryId,
        itemCategoryDescription = tContent.itemCategoryStr,
        ownershipStatusId = tContent.ownershipStatus,
        manufacturer = tContent.manufacturer.orEmpty(),
        model = tContent.model.orEmpty(),
        serialNumber = tContent.serialNumber.orEmpty(),
        ean = tContent.ean.orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(warehouseMovementId)
        parcel.writeLong(assetId)
        parcel.writeString(code)
        parcel.writeFloat(qty?.toFloat() ?: 0F)
        parcel.writeInt(contentStatusId)
        parcel.writeInt(assetStatusId)
        parcel.writeString(description)
        parcel.writeLong(warehouseAreaId)
        parcel.writeInt(labelNumber)
        parcel.writeValue(parentId)
        parcel.writeString(warehouseAreaDescription)
        parcel.writeString(warehouseDescription)
        parcel.writeLong(itemCategoryId)
        parcel.writeString(itemCategoryDescription)
        parcel.writeInt(ownershipStatusId)
        parcel.writeString(manufacturer)
        parcel.writeString(model)
        parcel.writeString(serialNumber)
        parcel.writeString(ean)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseMovementContent> {
        override fun createFromParcel(parcel: Parcel): WarehouseMovementContent {
            return WarehouseMovementContent(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseMovementContent?> {
            return arrayOfNulls(size)
        }
    }
}