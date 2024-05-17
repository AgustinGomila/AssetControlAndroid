package com.dacosys.assetControl.data.room.entity.review

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.review.AssetReview.Entry
import com.dacosys.assetControl.data.room.repository.review.AssetReviewContentRepository
import com.dacosys.assetControl.utils.Statics.Companion.toDate
import java.util.*

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.USER_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}"
        ),
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        )
    ]
)
data class AssetReview(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_REVIEW_DATE) var assetReviewDate: Date = Date(),
    @ColumnInfo(name = Entry.OBS) var obs: String? = null,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = Entry.MODIFICATION_DATE) var modificationDate: Date = Date(),
    @ColumnInfo(name = Entry.STATUS_ID) var statusId: Int = 0,
    @Ignore var warehouseAreaStr: String = "",
    @Ignore var warehouseStr: String = "",
    @Ignore var userStr: String = ""
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
    private var contentsRead: Boolean = false

    @Ignore
    private var mContents: ArrayList<AssetReviewContent> = arrayListOf()

    fun contents(): ArrayList<AssetReviewContent> {
        if (contentsRead) return mContents
        else {
            mContents = ArrayList(AssetReviewContentRepository().selectByAssetReviewId(this.id))
            contentsRead = true
            return mContents
        }
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        assetReviewDate = parcel.readString().orEmpty().toDate(),
        obs = parcel.readString(),
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
        parcel.writeLong(this.id)
        parcel.writeString(obs)
        parcel.writeLong(userId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeLong(warehouseId)
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
