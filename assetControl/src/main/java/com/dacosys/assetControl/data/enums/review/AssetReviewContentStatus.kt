package com.dacosys.assetControl.data.enums.review

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R

data class AssetReviewContentStatus(val id: Int, val description: String) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<AssetReviewContentStatus> {
        override fun createFromParcel(parcel: Parcel): AssetReviewContentStatus {
            return AssetReviewContentStatus(parcel)
        }

        override fun newArray(size: Int): Array<AssetReviewContentStatus?> {
            return arrayOfNulls(size)
        }

        var notInReview =
            AssetReviewContentStatus(id = 0, getContext().getString(R.string.review_cont_status_not_in_review))
        var revised = AssetReviewContentStatus(id = 1, getContext().getString(R.string.review_cont_status_revised))
        var external = AssetReviewContentStatus(id = 2, getContext().getString(R.string.review_cont_status_external))
        var unknown = AssetReviewContentStatus(id = 3, getContext().getString(R.string.review_cont_status_unknown))
        var appeared = AssetReviewContentStatus(4, getContext().getString(R.string.review_cont_status_appeared))
        var newAsset = AssetReviewContentStatus(id = 5, getContext().getString(R.string.review_cont_status_new))

        fun getAll(): List<AssetReviewContentStatus> {
            return listOf(
                notInReview,
                revised,
                external,
                unknown,
                appeared,
                newAsset
            )
        }

        fun getAllConfirm(): List<AssetReviewContentStatus> {
            return listOf(
                revised,
                external,
                unknown,
                appeared,
                newAsset
            )
        }

        fun getById(id: Int): AssetReviewContentStatus {
            return getAll().firstOrNull { it.id == id } ?: unknown
        }
    }
}