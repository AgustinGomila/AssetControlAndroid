package com.dacosys.assetControl.model.review

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import java.util.*

class AssetReviewContentStatus : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(assetReviewContentStatusId: Int, description: String) {
        this.description = description
        this.id = assetReviewContentStatusId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AssetReviewContentStatus) {
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

    companion object CREATOR : Parcelable.Creator<AssetReviewContentStatus> {
        override fun createFromParcel(parcel: Parcel): AssetReviewContentStatus {
            return AssetReviewContentStatus(parcel)
        }

        override fun newArray(size: Int): Array<AssetReviewContentStatus?> {
            return arrayOfNulls(size)
        }

        var notInReview = AssetReviewContentStatus(
            assetReviewContentStatusId = 0,
            getContext().getString(R.string.review_cont_status_not_in_review)
        )
        var revised = AssetReviewContentStatus(
            assetReviewContentStatusId = 1,
            getContext().getString(R.string.review_cont_status_revised)
        )
        var external = AssetReviewContentStatus(
            assetReviewContentStatusId = 2,
            getContext().getString(R.string.review_cont_status_external)
        )
        var unknown = AssetReviewContentStatus(
            assetReviewContentStatusId = 3,
            getContext().getString(R.string.review_cont_status_unknown)
        )
        var appeared = AssetReviewContentStatus(
            4,
            getContext().getString(R.string.review_cont_status_appeared)
        )
        var newAsset = AssetReviewContentStatus(
            assetReviewContentStatusId = 5,
            getContext().getString(R.string.review_cont_status_new)
        )

        fun getAllConfirm(): ArrayList<AssetReviewContentStatus> {
            val allSections = ArrayList<AssetReviewContentStatus>()
            Collections.addAll(
                allSections,
                revised,
                external,
                unknown,
                appeared,
                newAsset
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getAll(): ArrayList<AssetReviewContentStatus> {
            val allSections = ArrayList<AssetReviewContentStatus>()
            Collections.addAll(
                allSections,
                notInReview,
                revised,
                external,
                unknown,
                appeared,
                newAsset
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getAllIdAsString(): ArrayList<String> {
            val allSections = ArrayList<String>()
            Collections.addAll(
                allSections,
                notInReview.id.toString(),
                revised.id.toString(),
                external.id.toString(),
                unknown.id.toString(),
                appeared.id.toString(),
                newAsset.id.toString()
            )

            return ArrayList(allSections.sortedWith(compareBy { it }))
        }

        fun getById(assetReviewContentStatusId: Int): AssetReviewContentStatus? {
            return getAll().firstOrNull { it.id == assetReviewContentStatusId }
        }
    }
}