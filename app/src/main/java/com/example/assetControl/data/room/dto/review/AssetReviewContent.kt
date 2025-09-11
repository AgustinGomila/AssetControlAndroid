package com.example.assetControl.data.room.dto.review

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.entity.review.TempReviewContentEntity

abstract class AssetReviewContentEntry {
    companion object {
        const val TABLE_NAME = "asset_review_content"
        const val ID = "_id"
        const val ASSET_REVIEW_ID = "asset_review_id"
        const val ASSET_ID = "asset_id"
        const val CODE = "code"
        const val DESCRIPTION = "description"
        const val QTY = "qty"
        const val CONTENT_STATUS_ID = "content_status_id"
        const val ORIGIN_WAREHOUSE_AREA_ID = "origin_warehouse_area_id"

        const val ASSET_STATUS_ID = "asset_status_id"
        const val LABEL_NUMBER = "label_number"
        const val PARENT_ID = "parent_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
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

class AssetReviewContent(
    @ColumnInfo(name = AssetReviewContentEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = AssetReviewContentEntry.ASSET_REVIEW_ID) var assetReviewId: Long = 0L,
    @ColumnInfo(name = AssetReviewContentEntry.ASSET_ID) var assetId: Long = 0L,
    @ColumnInfo(name = AssetReviewContentEntry.CODE) var assetCode: String? = null,
    @ColumnInfo(name = AssetReviewContentEntry.DESCRIPTION) var assetDescription: String? = null,
    @ColumnInfo(name = AssetReviewContentEntry.QTY) var qty: Double? = null,
    @ColumnInfo(name = AssetReviewContentEntry.CONTENT_STATUS_ID) var contentStatusId: Int = 0,
    @ColumnInfo(name = AssetReviewContentEntry.ORIGIN_WAREHOUSE_AREA_ID) var originWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = AssetReviewContentEntry.ASSET_STATUS_ID) var assetStatusId: Int = 0,
    @ColumnInfo(name = AssetReviewContentEntry.LABEL_NUMBER) var labelNumber: Int = 0,
    @ColumnInfo(name = AssetReviewContentEntry.PARENT_ID) var parentId: Long? = null,
    @ColumnInfo(name = AssetReviewContentEntry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long = 0L,
    @ColumnInfo(name = AssetReviewContentEntry.WAREHOUSE_AREA_STR) var warehouseAreaDescription: String? = null,
    @ColumnInfo(name = AssetReviewContentEntry.WAREHOUSE_STR) var warehouseDescription: String? = null,
    @ColumnInfo(name = AssetReviewContentEntry.ITEM_CATEGORY_ID) var itemCategoryId: Long = 0L,
    @ColumnInfo(name = AssetReviewContentEntry.ITEM_CATEGORY_STR) var itemCategoryDescription: String? = null,
    @ColumnInfo(name = AssetReviewContentEntry.OWNERSHIP_STATUS_ID) var ownershipStatusId: Int = 0,
    @ColumnInfo(name = AssetReviewContentEntry.MANUFACTURER) var manufacturer: String? = null,
    @ColumnInfo(name = AssetReviewContentEntry.MODEL) var model: String? = null,
    @ColumnInfo(name = AssetReviewContentEntry.SERIAL_NUMBER) var serialNumber: String? = null,
    @ColumnInfo(name = AssetReviewContentEntry.EAN) var ean: String? = null,
) : Parcelable {

    @Ignore
    var code = assetCode.orEmpty()
        get() = assetCode.orEmpty()
        set(value) {
            assetCode = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var description = assetDescription.orEmpty()
        get() = assetDescription.orEmpty()
        set(value) {
            assetDescription = value.ifEmpty { null }
            field = value
        }

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

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        assetReviewId = parcel.readLong(),
        assetId = parcel.readLong(),
        assetCode = parcel.readString().orEmpty(),
        assetDescription = parcel.readString().orEmpty(),
        qty = parcel.readValue(Double::class.java.classLoader) as? Double,
        contentStatusId = parcel.readInt(),
        originWarehouseAreaId = parcel.readLong(),
        assetStatusId = parcel.readInt(),
        labelNumber = parcel.readInt(),
        parentId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseAreaId = parcel.readLong(),
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

    constructor(tContent: TempReviewContentEntity) : this(
        id = tContent.id,
        assetReviewId = tContent.assetReviewId,
        assetId = tContent.assetId,
        assetCode = tContent.code,
        qty = tContent.qty,
        contentStatusId = tContent.contentStatus,
        assetStatusId = tContent.status,
        assetDescription = tContent.description,
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

    constructor(
        assetReviewId: Long, id: Long, asset: Asset, qty: Double?, contentStatusId: Int, originWarehouseAreaId: Long
    ) : this(
        assetReviewId = assetReviewId,
        id = id,
        assetId = asset.id,
        assetCode = asset.code,
        assetDescription = asset.description,
        assetStatusId = asset.status,
        labelNumber = asset.labelNumber ?: 0,
        parentId = asset.parentId,
        warehouseAreaId = asset.warehouseAreaId,
        warehouseAreaDescription = asset.warehouseAreaStr,
        warehouseDescription = asset.warehouseStr,
        itemCategoryId = asset.itemCategoryId,
        itemCategoryDescription = asset.itemCategoryStr,
        ownershipStatusId = asset.status,
        manufacturer = asset.manufacturer.orEmpty(),
        model = asset.model.orEmpty(),
        serialNumber = asset.serialNumber.orEmpty(),
        ean = asset.ean.orEmpty(),
        qty = qty,
        contentStatusId = contentStatusId,
        originWarehouseAreaId = originWarehouseAreaId,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(assetReviewId)
        parcel.writeLong(assetId)
        parcel.writeString(assetCode)
        parcel.writeString(assetDescription)
        parcel.writeFloat(qty?.toFloat() ?: 0F)
        parcel.writeInt(contentStatusId)
        parcel.writeLong(originWarehouseAreaId)
        parcel.writeInt(assetStatusId)
        parcel.writeInt(labelNumber)
        parcel.writeValue(parentId)
        parcel.writeLong(warehouseAreaId)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssetReviewContent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<AssetReviewContent> {
        override fun createFromParcel(parcel: Parcel): AssetReviewContent {
            return AssetReviewContent(parcel)
        }

        override fun newArray(size: Int): Array<AssetReviewContent?> {
            return arrayOfNulls(size)
        }
    }
}