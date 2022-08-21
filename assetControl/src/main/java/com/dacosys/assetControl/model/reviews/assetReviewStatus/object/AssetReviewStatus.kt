package com.dacosys.assetControl.model.reviews.assetReviewStatus.`object`

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusContract
import java.util.*

class AssetReviewStatus : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(assetReviewStatusId: Int, description: String) {
        this.description = description
        this.id = assetReviewStatusId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AssetReviewStatus) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    constructor(parcel: Parcel) {
        id = parcel.readInt()
        description = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(AssetReviewStatusContract.AssetReviewStatusEntry.STATUS_ID, id)
        values.put(AssetReviewStatusContract.AssetReviewStatusEntry.DESCRIPTION, description)
        return values
    }

    companion object CREATOR : Parcelable.Creator<AssetReviewStatus> {
        override fun createFromParcel(parcel: Parcel): AssetReviewStatus {
            return AssetReviewStatus(parcel)
        }

        override fun newArray(size: Int): Array<AssetReviewStatus?> {
            return arrayOfNulls(size)
        }

        var unknown = AssetReviewStatus(
            0,
            Statics.AssetControl.getContext().getString(R.string.review_status_unknown)
        )
        var onProcess = AssetReviewStatus(
            1,
            Statics.AssetControl.getContext().getString(R.string.review_status_on_process)
        )
        var completed = AssetReviewStatus(
            2,
            Statics.AssetControl.getContext().getString(R.string.review_status_completed)
        )
        var transferred = AssetReviewStatus(
            3,
            Statics.AssetControl.getContext().getString(R.string.review_status_transferred)
        )

        fun getAll(): ArrayList<AssetReviewStatus> {
            val allSections = ArrayList<AssetReviewStatus>()
            Collections.addAll(
                allSections,
                unknown,
                onProcess,
                completed,
                transferred
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getAllIdAsString(): ArrayList<String> {
            val allSections = ArrayList<String>()
            Collections.addAll(
                allSections,
                unknown.id.toString(),
                onProcess.id.toString(),
                completed.id.toString(),
                transferred.id.toString()
            )

            return ArrayList(allSections.sortedWith(compareBy { it }))
        }

        fun getById(assetReviewStatusId: Int): AssetReviewStatus? {
            return getAll().firstOrNull { it.id == assetReviewStatusId }
        }
    }
}