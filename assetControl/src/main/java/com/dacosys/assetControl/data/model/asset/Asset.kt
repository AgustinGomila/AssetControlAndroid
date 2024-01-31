package com.dacosys.assetControl.data.model.asset

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.ACTIVE
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.ASSET_ID
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.CODE
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.CONDITION
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.EAN
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.ITEM_CATEGORY_ID
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.LABEL_NUMBER
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.LAST_ASSET_REVIEW_DATE
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.MANUFACTURER
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.MISSING_DATE
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.MODEL
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.ORIGINAL_WAREHOUSE_AREA_ID
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.ORIGINAL_WAREHOUSE_ID
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.OWNERSHIP_STATUS
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.PARENT_ID
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.SERIAL_NUMBER
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.STATUS
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.TRANSFERED
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.data.dataBase.asset.AssetContract.AssetEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.data.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.data.webservice.asset.AssetObject

class Asset : Parcelable {
    // setters
    var assetId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(aco: com.dacosys.assetControl.data.webservice.asset.AssetCollectorObject) {
        this.assetId = aco.asset_id
        this.code = aco.code
        this.description = aco.description
        this.warehouseId = aco.warehouse_id
        this.warehouseAreaId = aco.warehouse_area_id
        this.active = aco.active == 1
        this.ownershipStatusId = aco.ownership_status
        this.assetStatusId = aco.status
        this.missingDate = aco.missing_date
        this.itemCategoryId = aco.item_category_id
        this.transferred = false
        this.originalWarehouseId = aco.original_warehouse_id
        this.originalWarehouseAreaId = aco.original_warehouse_area_id
        this.labelNumber = aco.label_number
        this.manufacturer = ""
        this.model = ""
        this.serialNumber = aco.serial_number
        this.assetConditionId = aco.condition
        //this.costCentreId = aco.cost_centre_id
        this.parentAssetId = aco.parent_id
        this.ean = aco.ean
        this.lastAssetReviewDate = aco.last_asset_review_date

        dataRead = true
    }

    constructor(ao: AssetObject) {
        this.assetId = ao.asset_id
        this.code = ao.code
        this.description = ao.description
        this.warehouseId = ao.warehouse_id
        this.warehouseAreaId = ao.warehouse_area_id
        this.active = ao.active == 1
        this.ownershipStatusId = ao.ownership_status
        this.assetStatusId = ao.status
        this.missingDate = ao.missing_date
        this.itemCategoryId = ao.item_category_id
        this.transferred = false
        this.originalWarehouseId = ao.original_warehouse_id
        this.originalWarehouseAreaId = ao.original_warehouse_area_id
        this.labelNumber = ao.label_number
        this.manufacturer = ao.manufacturer
        this.model = ao.model
        this.serialNumber = ao.serial_number
        this.assetConditionId = ao.condition
        //this.costCentreId = ao.cost_centre_id
        this.parentAssetId = ao.parent_id
        this.ean = ao.ean
        this.lastAssetReviewDate = ao.last_asset_review_date

        dataRead = true
    }

    constructor(
        assetId: Long,
        code: String,
        description: String,
        warehouseId: Long,
        warehouseAreaId: Long,
        active: Boolean,
        ownershipStatus: Int,
        status: Int,
        missingDate: String?,
        itemCategoryId: Long,
        transferred: Boolean,
        originalWarehouseId: Long,
        originalWarehouseAreaId: Long,
        labelNumber: Int?,
        manufacturer: String?,
        model: String?,
        serialNumber: String?,
        condition: Int,
        //costCentreId: Long,
        parentId: Long,
        ean: String?,
        lastAssetReviewDate: String?,
    ) {
        this.assetId = assetId
        this.code = code
        this.description = description
        this.warehouseId = warehouseId
        this.warehouseAreaId = warehouseAreaId
        this.active = active
        this.ownershipStatusId = ownershipStatus
        this.assetStatusId = status
        this.missingDate = missingDate
        this.itemCategoryId = itemCategoryId
        this.transferred = transferred
        this.originalWarehouseId = originalWarehouseId
        this.originalWarehouseAreaId = originalWarehouseAreaId
        this.labelNumber = labelNumber
        this.manufacturer = manufacturer
        this.model = model
        this.serialNumber = serialNumber
        this.assetConditionId = condition
        //this.costCentreId = costCentreId
        this.parentAssetId = parentId
        this.ean = ean
        this.lastAssetReviewDate = lastAssetReviewDate

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        assetId = id
        dataRead = false
        if (doChecks) refreshData()
    }

    override fun toString(): String {
        return code
    }

    var description: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }
        set(value) {
            field = if (value.length > 255) {
                value.substring(0, 255)
            } else {
                value
            }
        }

    var ean: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }
        set(value) {
            field = if (value != null && value.length > 100) {
                value.substring(0, 100)
            } else {
                value
            }
        }

    var code: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }
        set(value) {
            field = if (value.length > 45) {
                value.substring(0, 45)
            } else {
                value
            }
        }

    var missingDate: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    var active: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    var itemCategoryId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var itemCategoryStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    val assetStatus: AssetStatus?
        get() {
            return AssetStatus.getById(assetStatusId)
        }

    var assetStatusId: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    val ownershipStatus: OwnershipStatus?
        get() {
            return OwnershipStatus.getById(ownershipStatusId)
        }

    var ownershipStatusId: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var warehouseId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var warehouseStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var warehouseAreaId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var warehouseAreaStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var transferred: Boolean? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    var originalWarehouseId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var originalWarehouseStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var originalWarehouseAreaId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var originalWarehouseAreaStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var labelNumber: Int? = 0
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    var serialNumber: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }
        set(value) {
            field = if (value != null && value.length > 100) {
                value.substring(0, 100)
            } else {
                value
            }
        }

    var manufacturer: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }
        set(value) {
            field = if (value != null && value.length > 255) {
                value.substring(0, 255)
            } else {
                value
            }
        }

    var model: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }
        set(value) {
            field = if (value != null && value.length > 255) {
                value.substring(0, 255)
            } else {
                value
            }
        }

    var parentAssetId: Long? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    val assetCondition: AssetCondition?
        get() {
            return AssetCondition.getById(assetConditionId)
        }

    var assetConditionId: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var lastAssetReviewDate: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    constructor()

    constructor(parcel: Parcel) {
        active = parcel.readByte() != 0.toByte()
        assetConditionId = parcel.readInt()
        assetId = parcel.readLong()
        assetStatusId = parcel.readInt()
        code = parcel.readString() ?: ""
        description = parcel.readString() ?: ""
        ean = parcel.readString()
        itemCategoryId = parcel.readLong()
        labelNumber = parcel.readInt()
        lastAssetReviewDate = parcel.readString()
        manufacturer = parcel.readString()
        missingDate = parcel.readString()
        model = parcel.readString()
        originalWarehouseAreaId = parcel.readLong()
        originalWarehouseId = parcel.readLong()
        ownershipStatusId = parcel.readInt()
        parentAssetId = parcel.readLong()
        serialNumber = parcel.readString()
        transferred = parcel.readByte() != 0.toByte()
        warehouseAreaId = parcel.readLong()
        warehouseId = parcel.readLong()

        itemCategoryStr = parcel.readString() ?: ""
        warehouseStr = parcel.readString() ?: ""
        warehouseAreaStr = parcel.readString() ?: ""
        originalWarehouseStr = parcel.readString() ?: ""
        originalWarehouseAreaStr = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(ACTIVE, active)
        values.put(CONDITION, assetConditionId)
        values.put(ASSET_ID, assetId)
        values.put(STATUS, assetStatusId)
        values.put(CODE, code)
        //values.put(COST_CENTRE_ID, costCentreId)
        values.put(DESCRIPTION, description)
        values.put(EAN, ean)
        values.put(ITEM_CATEGORY_ID, itemCategoryId)
        values.put(LABEL_NUMBER, labelNumber)
        values.put(LAST_ASSET_REVIEW_DATE, if (lastAssetReviewDate == null) "" else lastAssetReviewDate.toString())
        values.put(MANUFACTURER, if (manufacturer == null) "" else manufacturer)
        values.put(MISSING_DATE, if (missingDate == null) "" else missingDate.toString())
        values.put(MODEL, if (model == null) "" else model)
        values.put(ORIGINAL_WAREHOUSE_AREA_ID, originalWarehouseAreaId)
        values.put(ORIGINAL_WAREHOUSE_ID, originalWarehouseId)
        values.put(OWNERSHIP_STATUS, ownershipStatusId)
        values.put(PARENT_ID, parentAssetId)
        values.put(SERIAL_NUMBER, if (serialNumber == null) "" else serialNumber)
        values.put(TRANSFERED, transferred)
        values.put(WAREHOUSE_AREA_ID, warehouseAreaId)
        values.put(WAREHOUSE_ID, warehouseId)

        /*
        values.put(ITEM_CATEGORY_STR, itemCategoryStr)
        values.put(WAREHOUSE_STR, warehouseStr)
        values.put(WAREHOUSE_AREA_STR, warehouseAreaStr)
        values.put(ORIGINAL_WAREHOUSE_ID, originalWarehouseStr)
        values.put(ORIGINAL_WAREHOUSE_AREA_STR, originalWarehouseStr)
        */

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return AssetDbHelper().update(this)
    }

    private fun refreshData(): Boolean {
        val temp = AssetDbHelper().selectById(this.assetId) ?: return false

        assetId = temp.assetId
        code = temp.code
        description = temp.description
        warehouseId = temp.warehouseId
        warehouseAreaId = temp.warehouseAreaId
        active = temp.active
        ownershipStatusId = temp.ownershipStatusId
        assetStatusId = temp.assetStatusId
        missingDate = temp.missingDate
        itemCategoryId = temp.itemCategoryId
        transferred = temp.transferred
        originalWarehouseId = temp.originalWarehouseId
        originalWarehouseAreaId = temp.originalWarehouseAreaId
        labelNumber = temp.labelNumber
        manufacturer = temp.manufacturer
        model = temp.model
        serialNumber = temp.serialNumber
        assetConditionId = temp.assetConditionId
        //costCentreId = temp.costCentreId
        parentAssetId = temp.parentAssetId
        ean = temp.ean
        lastAssetReviewDate = temp.lastAssetReviewDate

        itemCategoryStr = temp.itemCategoryStr
        warehouseStr = temp.warehouseStr
        warehouseAreaStr = temp.warehouseAreaStr
        originalWarehouseStr = temp.originalWarehouseStr
        originalWarehouseAreaStr = temp.originalWarehouseAreaStr
        //costCentreStr = temp.costCentreStr

        /*
        assetStatusStr= temp.assetStatusStr
        ownershipStatusStr= temp.ownershipStatusStr
        assetConditionStr=temp.assetConditionStr
        */

        dataRead = true

        return true
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Asset) {
            false
        } else this.assetId == other.assetId
    }

    override fun hashCode(): Int {
        return this.assetId.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        /*
        active = parcel.readByte() != 0.toByte()
        assetConditionId = parcel.readLong()
        assetId = parcel.readLong()
        assetStatusId = parcel.readInt()
        code = parcel.readString()
        description = parcel.readString()
        ean = parcel.readString()
        itemCategoryId = parcel.readLong()
        labelNumber = parcel.readInt()
        lastAssetReviewDate = parcel.readString()
        manufacturer = parcel.readString()
        missingDate = parcel.readString()
        model = parcel.readString()
        originalWarehouseAreaId = parcel.readLong()
        originalWarehouseId = parcel.readLong()
        ownershipStatusId = parcel.readInt()
        parentAssetId = parcel.readLong()
        serialNumber = parcel.readString()
        transferred = parcel.readByte() != 0.toByte()
        warehouseAreaId = parcel.readLong()
        warehouseId = parcel.readLong()
         */

        parcel.writeByte(if (active) 1 else 0)
        parcel.writeInt(assetConditionId)
        parcel.writeLong(assetId)
        parcel.writeInt(assetStatusId)
        parcel.writeString(code)
        parcel.writeString(description)
        parcel.writeString(ean)
        parcel.writeLong(itemCategoryId)
        parcel.writeInt(if (labelNumber == null) 0 else labelNumber ?: return)
        parcel.writeString(lastAssetReviewDate)
        parcel.writeString(if (manufacturer == null) "" else manufacturer)
        parcel.writeString(missingDate)
        parcel.writeString(if (model == null) "" else model)
        parcel.writeLong(originalWarehouseAreaId)
        parcel.writeLong(originalWarehouseId)
        parcel.writeInt(ownershipStatusId)
        parcel.writeLong(if (parentAssetId == null) 0 else parentAssetId ?: return)
        parcel.writeString(if (serialNumber == null) "" else serialNumber)
        parcel.writeInt(if (transferred == null) 0 else if (transferred ?: return) 1 else 0)
        parcel.writeLong(warehouseAreaId)
        parcel.writeLong(warehouseId)
        parcel.writeString(itemCategoryStr)
        parcel.writeString(warehouseStr)
        parcel.writeString(warehouseAreaStr)
        parcel.writeString(originalWarehouseStr)
        parcel.writeString(originalWarehouseAreaStr)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
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