package com.dacosys.assetControl.data.model.movement

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.ASSET_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.CODE
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.QTY
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.WAREHOUSE_MOVEMENT_CONTENT_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.WAREHOUSE_MOVEMENT_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContentDbHelper
import com.dacosys.assetControl.data.model.asset.Asset

class WarehouseMovementContent : Parcelable {
    var warehouseMovementContentId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        warehouseMovementId: Long,
        warehouseMovementContentId: Long,
        asset: Asset,
        contentStatusId: Int,
    ) {
        this.warehouseMovementId = warehouseMovementId
        this.warehouseMovementContentId = warehouseMovementContentId
        this.collectorContentId = warehouseMovementContentId
        this.contentStatusId = contentStatusId
        this.assetId = asset.assetId
        this.code = asset.code
        this.qty = 1F
        this.description = asset.description
        this.warehouseAreaId = asset.warehouseAreaId
        this.warehouseAreaStr = asset.warehouseAreaStr
        this.warehouseStr = asset.warehouseStr
        this.ownershipStatusId = asset.ownershipStatusId
        this.assetStatusId = asset.assetStatusId
        this.itemCategoryId = asset.itemCategoryId
        this.itemCategoryStr = asset.itemCategoryStr
        this.labelNumber = asset.labelNumber ?: 0
        this.manufacturer = asset.manufacturer ?: ""
        this.model = asset.model ?: ""
        this.serialNumber = asset.serialNumber ?: ""
        this.parentId = asset.parentAssetId ?: 0
        this.ean = asset.ean ?: ""

        dataRead = true
    }

    constructor(
        warehouseMovementId: Long,
        warehouseMovementContentId: Long,
        assetId: Long,
        code: String,
        qty: Float,
    ) {
        this.warehouseMovementId = warehouseMovementId
        this.warehouseMovementContentId = warehouseMovementContentId
        this.collectorContentId = warehouseMovementContentId
        this.assetId = assetId
        this.code = code
        this.qty = qty

        dataRead = true
    }

    constructor(
        warehouseMovementId: Long,
        warehouseMovementContentId: Long,
        contentStatus: Int,
        assetId: Long,
        code: String,
        qty: Float,
        description: String,
        warehouseAreaId: Long,
        warehouseAreaStr: String,
        warehouseStr: String,
        ownershipStatusId: Int,
        assetStatusId: Int,
        itemCategoryId: Long,
        itemCategoryStr: String,
        labelNumber: Int,
        manufacturer: String,
        model: String,
        serialNumber: String,
        parentId: Long,
        ean: String
    ) {
        this.warehouseMovementId = warehouseMovementId
        this.warehouseMovementContentId = warehouseMovementContentId
        this.contentStatusId = contentStatus
        this.collectorContentId = warehouseMovementContentId
        this.assetId = assetId
        this.code = code
        this.qty = qty
        this.description = description
        this.warehouseAreaId = warehouseAreaId
        this.warehouseAreaStr = warehouseAreaStr
        this.warehouseStr = warehouseStr
        this.ownershipStatusId = ownershipStatusId
        this.assetStatusId = assetStatusId
        this.itemCategoryId = itemCategoryId
        this.itemCategoryStr = itemCategoryStr
        this.labelNumber = labelNumber
        this.manufacturer = manufacturer
        this.model = model
        this.serialNumber = serialNumber
        this.parentId = parentId
        this.ean = ean

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        warehouseMovementContentId = id
        collectorContentId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp = WarehouseMovementContentDbHelper().selectById(this.warehouseMovementContentId)

        dataRead = true
        return when {
            temp != null -> {
                this.warehouseMovementId = temp.warehouseMovementId
                this.warehouseMovementContentId = temp.warehouseMovementContentId
                this.collectorContentId = temp.collectorContentId
                this.contentStatusId = temp.contentStatusId

                this.assetId = temp.assetId
                this.code = temp.code
                this.description = temp.description
                this.assetStatusId = temp.assetStatusId
                this.warehouseAreaId = temp.warehouseAreaId
                this.labelNumber = temp.labelNumber
                this.parentId = temp.parentId
                this.qty = temp.qty

                true
            }

            else -> false
        }
    }

    override fun toString(): String {
        return code
    }

    val asset: Asset?
        get() {
            return if (assetId == 0L) null
            else Asset(assetId, false)
        }

    private var warehouseMovementId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var assetId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var code: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var qty: Float = 0F
        get() {
            return if (!dataRead && !refreshData()) 0F
            else field
        }

    /*
    Campos especiales para movimientos
    */

    var assetStatusId: Int = 0
    var description: String = ""
    var collectorContentId: Long = 0
    var labelNumber: Int = 0
    var parentId: Long = 0
    var warehouseAreaId: Long = 0
    var contentStatusId: Int = 0
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
        this.warehouseMovementId = parcel.readLong()
        this.warehouseMovementContentId = parcel.readLong()
        this.collectorContentId = warehouseMovementContentId
        this.contentStatusId = parcel.readInt()

        this.assetId = parcel.readLong()
        this.code = parcel.readString() ?: ""
        this.description = parcel.readString() ?: ""
        this.assetStatusId = parcel.readInt()
        this.warehouseAreaId = parcel.readLong()
        this.labelNumber = parcel.readInt()
        this.parentId = parcel.readLong()
        this.qty = parcel.readFloat()

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

        values.put(WAREHOUSE_MOVEMENT_ID, warehouseMovementId)
        values.put(WAREHOUSE_MOVEMENT_CONTENT_ID, warehouseMovementContentId)
        values.put(ASSET_ID, assetId)
        values.put(CODE, code)
        values.put(QTY, qty)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return WarehouseMovementContentDbHelper().update(this)
    }

    fun equals(a: Any?, b: Any?): Boolean {
        return a != null && b != null && a == b
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is WarehouseMovementContent) {
            false
        } else equals(this.assetId, other.assetId) &&
                equals(this.warehouseMovementId, other.warehouseMovementId)
    }

    override fun hashCode(): Int {
        return this.assetId.hashCode().hashCode()
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(warehouseMovementId)
        parcel.writeLong(warehouseMovementContentId)
        parcel.writeInt(contentStatusId)

        parcel.writeLong(assetId)
        parcel.writeString(code)
        parcel.writeString(description)
        parcel.writeInt(assetStatusId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeInt(labelNumber)
        parcel.writeLong(parentId)
        parcel.writeFloat(qty)

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

    companion object CREATOR : Parcelable.Creator<WarehouseMovementContent> {
        override fun createFromParcel(parcel: android.os.Parcel): WarehouseMovementContent {
            return WarehouseMovementContent(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseMovementContent?> {
            return arrayOfNulls(size)
        }
    }
}