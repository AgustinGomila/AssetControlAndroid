package com.example.assetControl.data.room.dto.review

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.example.assetControl.data.room.repository.review.AssetReviewContentRepository
import com.example.assetControl.data.room.repository.review.AssetReviewRepository
import java.util.*

abstract class AssetReviewEntry {
    companion object {
        const val TABLE_NAME = "asset_review"
        const val ID = "_id"
        const val ASSET_REVIEW_DATE = "asset_review_date"
        const val OBS = "obs"
        const val USER_ID = "user_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val WAREHOUSE_ID = "warehouse_id"
        const val MODIFICATION_DATE = "modification_date"
        const val STATUS_ID = "status_id"

        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        const val WAREHOUSE_STR = "warehouse_str"
        const val USER_STR = "user_str"
    }
}

class AssetReview(
    @ColumnInfo(name = AssetReviewEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = AssetReviewEntry.ASSET_REVIEW_DATE) var assetReviewDate: Date = Date(),
    @ColumnInfo(name = AssetReviewEntry.OBS) var observations: String? = null,
    @ColumnInfo(name = AssetReviewEntry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = AssetReviewEntry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long = 0L,
    @ColumnInfo(name = AssetReviewEntry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = AssetReviewEntry.MODIFICATION_DATE) var modificationDate: Date = Date(),
    @ColumnInfo(name = AssetReviewEntry.STATUS_ID) var statusId: Int = 0,
    @ColumnInfo(name = AssetReviewEntry.WAREHOUSE_AREA_STR) var warehouseAreaStr: String? = null,
    @ColumnInfo(name = AssetReviewEntry.WAREHOUSE_STR) var warehouseStr: String? = null,
    @ColumnInfo(name = AssetReviewEntry.USER_STR) var userStr: String = ""
) : Parcelable {

    fun saveChanges() = AssetReviewRepository().update(this)

    @Ignore
    var obs: String = observations.orEmpty()
        get() = observations.orEmpty()
        set(value) {
            observations = value.ifEmpty { null }
            field = value
        }

    @Ignore
    private var contentsRead: Boolean = false

    @Ignore
    private var mContents: ArrayList<AssetReviewContent> = arrayListOf()

    fun contents(): ArrayList<AssetReviewContent> {
        if (contentsRead) return mContents
        else {
            mContents = ArrayList(AssetReviewContentRepository().selectByAssetReviewId(id))
            contentsRead = true
            return mContents
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssetReview

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        assetReviewDate = Date(parcel.readLong()),
        observations = parcel.readString(),
        userId = parcel.readLong(),
        warehouseAreaId = parcel.readLong(),
        warehouseId = parcel.readLong(),
        modificationDate = Date(parcel.readLong()),
        statusId = parcel.readInt(),
        warehouseAreaStr = parcel.readString().orEmpty(),
        warehouseStr = parcel.readString().orEmpty(),
        userStr = parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(assetReviewDate.time)
        parcel.writeString(observations)
        parcel.writeLong(userId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeLong(warehouseId)
        parcel.writeLong(modificationDate.time)
        parcel.writeInt(statusId)
        parcel.writeString(warehouseAreaStr)
        parcel.writeString(warehouseStr)
        parcel.writeString(userStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetReview> {
        override fun createFromParcel(parcel: Parcel): AssetReview {
            return AssetReview(parcel)
        }

        override fun newArray(size: Int): Array<AssetReview?> {
            return arrayOfNulls(size)
        }
    }
}
