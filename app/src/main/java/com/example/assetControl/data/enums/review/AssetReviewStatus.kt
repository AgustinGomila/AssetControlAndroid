package com.example.assetControl.data.enums.review

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R

data class AssetReviewStatus(val id: Int, val description: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetReviewStatus> {
        override fun createFromParcel(parcel: Parcel): AssetReviewStatus {
            return AssetReviewStatus(parcel)
        }

        override fun newArray(size: Int): Array<AssetReviewStatus?> {
            return arrayOfNulls(size)
        }

        var unknown = AssetReviewStatus(0, context.getString(R.string.review_status_unknown))
        var onProcess = AssetReviewStatus(1, context.getString(R.string.review_status_on_process))
        var completed = AssetReviewStatus(2, context.getString(R.string.review_status_completed))
        var transferred =
            AssetReviewStatus(3, context.getString(R.string.review_status_transferred))

        fun getAll(): List<AssetReviewStatus> {
            return listOf(unknown, onProcess, completed, transferred)
        }

        fun getById(id: Int): AssetReviewStatus {
            return getAll().firstOrNull { it.id == id } ?: unknown
        }
    }
}