package com.dacosys.assetControl.data.model.review

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContentDbHelper
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContract.AssetReviewEntry.Companion.ASSET_REVIEW_DATE
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContract.AssetReviewEntry.Companion.ASSET_REVIEW_ID
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContract.AssetReviewEntry.Companion.COLLECTOR_ASSET_REVIEW_ID
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContract.AssetReviewEntry.Companion.MODIFICATION_DATE
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContract.AssetReviewEntry.Companion.OBS
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContract.AssetReviewEntry.Companion.STATUS_ID
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContract.AssetReviewEntry.Companion.USER_ID
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContract.AssetReviewEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.data.dataBase.review.AssetReviewContract.AssetReviewEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.data.dataBase.review.AssetReviewDbHelper

class AssetReview : Parcelable {
    var collectorAssetReviewId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        assetReviewId: Long,
        assetReviewDate: String,
        obs: String,
        userId: Long,
        warehouseAreaId: Long,
        warehouseId: Long,
        modificationDate: String,
        collectorAssetReviewId: Long,
        statusId: Int,
    ) {
        this.assetReviewId = assetReviewId
        this.assetReviewDate = assetReviewDate
        this.obs = obs
        this.userId = userId
        this.warehouseAreaId = warehouseAreaId
        this.warehouseId = warehouseId
        this.modificationDate = modificationDate
        this.collectorAssetReviewId = collectorAssetReviewId
        this.statusId = statusId

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        collectorAssetReviewId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp = AssetReviewDbHelper().selectById(this.collectorAssetReviewId)

        dataRead = true
        return when {
            temp != null -> {
                this.assetReviewId = temp.assetReviewId
                this.assetReviewDate = temp.assetReviewDate
                this.obs = temp.obs
                this.userId = temp.userId
                this.warehouseAreaId = temp.warehouseAreaId
                this.warehouseId = temp.warehouseId
                this.modificationDate = temp.modificationDate
                this.collectorAssetReviewId = temp.collectorAssetReviewId
                this.statusId = temp.statusId

                this.userStr = temp.userStr
                this.warehouseAreaStr = temp.warehouseAreaStr
                this.warehouseStr = temp.warehouseStr

                true
            }

            else -> false
        }
    }

    val contents: ArrayList<AssetReviewContent>
        get() {
            return AssetReviewContentDbHelper().selectByAssetReviewCollectorId(
                this.collectorAssetReviewId
            )
        }

    var assetReviewDate: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var modificationDate: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var obs: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    private var assetReviewId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    /*
    val user: User?
    get() {
        return when {
            userId == 0 -> null
            else -> User( userId, false)
        }
    }
    */

    var userId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var userStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    /*
    val warehouseArea: WarehouseArea?
    get() {
        return when {
            warehouseAreaId == 0 -> null
            else -> WarehouseArea( warehouseAreaId, false)
        }
    }
    */

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

    /*
    val warehouse: Warehouse?
    get() {
        return when {
            warehouseId == 0 -> null
            else -> Warehouse( warehouseId, false)
        }
    }
    */

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

    var statusId: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    constructor(parcel: android.os.Parcel) {
        assetReviewId = parcel.readLong()
        assetReviewDate = parcel.readString() ?: ""
        obs = parcel.readString() ?: ""
        userId = parcel.readLong()
        warehouseAreaId = parcel.readLong()
        warehouseId = parcel.readLong()
        modificationDate = parcel.readString() ?: ""
        collectorAssetReviewId = parcel.readLong()
        statusId = parcel.readInt()

        userStr = parcel.readString() ?: ""
        warehouseAreaStr = parcel.readString() ?: ""
        warehouseStr = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(ASSET_REVIEW_ID, assetReviewId)
        values.put(ASSET_REVIEW_DATE, assetReviewDate)
        values.put(OBS, obs)
        values.put(USER_ID, userId)
        values.put(WAREHOUSE_AREA_ID, warehouseAreaId)
        values.put(WAREHOUSE_ID, warehouseId)
        values.put(MODIFICATION_DATE, modificationDate)
        values.put(COLLECTOR_ASSET_REVIEW_ID, collectorAssetReviewId)
        values.put(STATUS_ID, statusId)

        /*
        values.put(USER_STR, userStr)
        values.put(WAREHOUSE_AREA_STR, warehouseAreaStr)
        values.put(WAREHOUSE_STR, warehouseStr)
        */

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return AssetReviewDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AssetReview) {
            false
        } else equals(this.collectorAssetReviewId, other.collectorAssetReviewId)
    }

    override fun hashCode(): Int {
        return this.collectorAssetReviewId.hashCode()
    }

    class CustomComparator : Comparator<AssetReview> {
        override fun compare(o1: AssetReview, o2: AssetReview): Int {
            if (o1.collectorAssetReviewId < o2.collectorAssetReviewId) {
                return -1
            } else if (o1.collectorAssetReviewId > o2.collectorAssetReviewId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(assetReviewId)
        parcel.writeString(assetReviewDate)
        parcel.writeString(obs)
        parcel.writeLong(userId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeLong(warehouseId)
        parcel.writeString(modificationDate)
        parcel.writeLong(collectorAssetReviewId)
        parcel.writeInt(statusId)

        parcel.writeString(userStr)
        parcel.writeString(warehouseAreaStr)
        parcel.writeString(warehouseStr)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetReview> {
        override fun createFromParcel(parcel: android.os.Parcel): AssetReview {
            return AssetReview(parcel)
        }

        override fun newArray(size: Int): Array<AssetReview?> {
            return arrayOfNulls(size)
        }
    }
}