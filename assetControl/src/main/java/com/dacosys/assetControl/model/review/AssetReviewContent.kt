package com.dacosys.assetControl.model.review

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ASSET_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ASSET_REVIEW_CONTENT_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ASSET_REVIEW_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.CODE
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.CONTENT_STATUS_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ORIGIN_WAREHOUSE_AREA_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.QTY
import com.dacosys.assetControl.dataBase.review.AssetReviewContentDbHelper
import com.dacosys.assetControl.model.asset.Asset

class AssetReviewContent : Parcelable {
    var assetReviewContentId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        assetReviewId: Long,
        assetReviewContentId: Long,
        assetId: Long,
        code: String,
        description: String,
        qty: Float,
        contentStatusId: Int,
        originWarehouseAreaId: Long,
    ) {
        this.assetReviewId = assetReviewId
        this.assetReviewContentId = assetReviewContentId
        this.assetId = assetId
        this.code = code
        this.description = description
        this.qty = qty
        this.contentStatusId = contentStatusId
        this.originWarehouseAreaId = originWarehouseAreaId

        dataRead = true
    }

    constructor(id: Long, asset: Asset, doChecks: Boolean) {
        assetReviewContentId = id
        assetId = asset.assetId

        if (doChecks) {
            refreshData()
        }
    }

    constructor()

    constructor(id: Long, doChecks: Boolean) {
        assetReviewContentId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = AssetReviewContentDbHelper().selectById(this.assetReviewContentId)

        dataRead = true
        return when {
            temp != null -> {
                this.assetReviewId = temp.assetReviewId
                this.assetReviewContentId = temp.assetReviewContentId
                this.contentStatusId = temp.contentStatusId

                this.assetId = temp.assetId
                this.code = temp.code
                this.description = temp.description
                this.qty = temp.qty
                this.originWarehouseAreaId = temp.originWarehouseAreaId

                true
            }
            else -> false
        }
    }

    override fun toString(): String {
        return description
    }

    /*
    val assetReview: AssetReview?
        get() {
            return if (assetReviewId == 0L) {
                null
            } else AssetReview( assetReviewId, false)
        }

    val asset: Asset?
        get() {
            return if (assetId == 0L) {
                null
            } else Asset( assetId, false)
        }

    val originWarehouseArea: WarehouseArea?
        get() {
            return if (originWarehouseAreaId == 0L) {
                null
            } else WarehouseArea( originWarehouseAreaId, false)
        }
    */

    var assetReviewId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var assetId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var originWarehouseAreaId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var code: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    var description: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    var qty: Float = 0F
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0F
                }
            }
            return field
        }

    var contentStatusId: Int = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    /*
    Campos especiales para revisiones
    */

    var assetStatusId: Int = 0
    var collectorContentId: Long = 0
    var labelNumber: Int = 0
    var parentId: Long = 0
    var warehouseAreaId: Long = 0
    var warehouseAreaStr: String = ""
    var warehouseStr: String = ""
    var itemCategoryId: Long = 0
    var itemCategoryStr: String = ""
    var ownershipStatusId: Int = 0
    var manufacturer: String = ""
    var model: String = ""
    var serialNumber: String = ""
    var ean: String = ""

    constructor(parcel: android.os.Parcel) {
        this.assetReviewId = parcel.readLong()
        this.assetReviewContentId = parcel.readLong()
        this.assetId = parcel.readLong()
        this.code = parcel.readString() ?: ""
        this.description = parcel.readString() ?: ""
        this.qty = parcel.readFloat()
        this.contentStatusId = parcel.readInt()
        this.originWarehouseAreaId = parcel.readLong()

        this.assetStatusId = parcel.readInt()
        this.collectorContentId = parcel.readLong()
        this.labelNumber = parcel.readInt()
        this.parentId = parcel.readLong()
        this.warehouseAreaId = parcel.readLong()
        this.warehouseAreaStr = parcel.readString() ?: ""
        this.warehouseStr = parcel.readString() ?: ""
        this.itemCategoryId = parcel.readLong()
        this.itemCategoryStr = parcel.readString() ?: ""
        this.ownershipStatusId = parcel.readInt()
        this.manufacturer = parcel.readString() ?: ""
        this.model = parcel.readString() ?: ""
        this.serialNumber = parcel.readString() ?: ""
        this.ean = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(ASSET_REVIEW_ID, assetReviewId)
        values.put(ASSET_REVIEW_CONTENT_ID, assetReviewContentId)
        values.put(ASSET_ID, assetId)
        values.put(CODE, code)
        values.put(DESCRIPTION, description)
        values.put(QTY, qty)
        values.put(CONTENT_STATUS_ID, contentStatusId)
        values.put(ORIGIN_WAREHOUSE_AREA_ID, originWarehouseAreaId)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return AssetReviewContentDbHelper().update(this)
    }

    fun equals(a: Any?, b: Any?): Boolean {
        return a != null && b != null && a == b
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AssetReviewContent) {
            false
        } else
            equals(this.assetReviewId, other.assetReviewId) &&
                    equals(this.assetReviewContentId, other.assetReviewContentId) &&
                    equals(this.assetId, other.assetId)
    }

    override fun hashCode(): Int {
        return this.assetReviewContentId.hashCode()
    }

    class CustomComparator : Comparator<AssetReviewContent> {
        override fun compare(o1: AssetReviewContent, o2: AssetReviewContent): Int {
            if (o1.assetReviewContentId < o2.assetReviewContentId) {
                return -1
            } else if (o1.assetReviewContentId > o2.assetReviewContentId) {
                return 1
            }
            return 0
        }
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(assetReviewId)
        parcel.writeLong(assetReviewContentId)
        parcel.writeLong(assetId)
        parcel.writeString(code)
        parcel.writeString(description)
        parcel.writeFloat(qty)
        parcel.writeInt(contentStatusId)
        parcel.writeLong(originWarehouseAreaId)

        parcel.writeInt(assetStatusId)
        parcel.writeLong(collectorContentId)
        parcel.writeInt(labelNumber)
        parcel.writeLong(parentId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeString(warehouseAreaStr)
        parcel.writeString(warehouseStr)
        parcel.writeLong(itemCategoryId)
        parcel.writeString(itemCategoryStr)
        parcel.writeInt(ownershipStatusId)
        parcel.writeString(manufacturer)
        parcel.writeString(model)
        parcel.writeString(serialNumber)
        parcel.writeString(ean)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetReviewContent> {
        override fun createFromParcel(parcel: android.os.Parcel): AssetReviewContent {
            return AssetReviewContent(parcel)
        }

        override fun newArray(size: Int): Array<AssetReviewContent?> {
            return arrayOfNulls(size)
        }

        fun add(
            assetReviewId: Long,
            assetReviewContentId: Long,
            assetId: Long,
            code: String,
            description: String,
            qty: Float,
            contentStatusId: Int,
            originWarehouseAreaId: Long,
        ): AssetReviewContent? {
            if (description.isEmpty()) {
                return null
            }

            val i = AssetReviewContentDbHelper()
            val ok = i.insert(
                assetReviewId,
                assetReviewContentId,
                assetId,
                code,
                description,
                qty,
                contentStatusId,
                originWarehouseAreaId
            )

            return if (ok) i.selectById(assetReviewContentId) else null
        }
    }
}