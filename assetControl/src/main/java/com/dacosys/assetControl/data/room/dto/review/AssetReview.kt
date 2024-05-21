package com.dacosys.assetControl.data.room.dto.review

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.room.repository.review.AssetReviewContentRepository
import com.dacosys.assetControl.utils.Statics.Companion.toDate
import java.util.*

class AssetReview(
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_REVIEW_DATE) var assetReviewDate: Date = Date(),
    @ColumnInfo(name = Entry.OBS) var observations: String? = null,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = Entry.MODIFICATION_DATE) var modificationDate: Date = Date(),
    @ColumnInfo(name = Entry.STATUS_ID) var statusId: Int = 0,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_STR) var warehouseAreaStr: String = "",
    @ColumnInfo(name = Entry.WAREHOUSE_STR) var warehouseStr: String = "",
    @ColumnInfo(name = Entry.USER_STR) var userStr: String = ""
) : Parcelable {
    object Entry {
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

    @Ignore
    var obs: String = observations.orEmpty()

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

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        assetReviewDate = parcel.readString().orEmpty().toDate(),
        observations = parcel.readString(),
        userId = parcel.readLong(),
        warehouseAreaId = parcel.readLong(),
        warehouseId = parcel.readLong(),
        modificationDate = parcel.readString().orEmpty().toDate(),
        statusId = parcel.readInt(),
        warehouseAreaStr = parcel.readString().orEmpty(),
        warehouseStr = parcel.readString().orEmpty(),
        userStr = parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(assetReviewDate.toString())
        parcel.writeString(observations)
        parcel.writeLong(userId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeLong(warehouseId)
        parcel.writeString(modificationDate.toString())
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
