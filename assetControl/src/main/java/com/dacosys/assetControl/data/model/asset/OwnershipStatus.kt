package com.dacosys.assetControl.data.model.asset

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import java.util.*

class OwnershipStatus : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(ownershipStatusId: Int, description: String) {
        this.description = description
        this.id = ownershipStatusId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is OwnershipStatus) {
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

    companion object CREATOR : Parcelable.Creator<OwnershipStatus> {
        override fun createFromParcel(parcel: Parcel): OwnershipStatus {
            return OwnershipStatus(parcel)
        }

        override fun newArray(size: Int): Array<OwnershipStatus?> {
            return arrayOfNulls(size)
        }

        var unknown = OwnershipStatus(
            0,
            getContext().getString(R.string.ownership_status_unknown)
        )
        var owned = OwnershipStatus(
            1,
            getContext().getString(R.string.ownership_status_owned)
        )
        private var rented = OwnershipStatus(
            2,
            getContext().getString(R.string.ownership_status_rented)
        )
        private var leased = OwnershipStatus(
            3,
            getContext().getString(R.string.ownership_status_leased)
        )
        private var someoneElse = OwnershipStatus(
            4,
            getContext().getString(R.string.ownership_status_someone_else)
        )

        fun getAll(): ArrayList<OwnershipStatus> {
            val allSections = ArrayList<OwnershipStatus>()
            Collections.addAll(
                allSections,
                unknown,
                owned,
                rented,
                leased,
                someoneElse
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(ownershipStatusId: Int): OwnershipStatus? {
            return getAll().firstOrNull { it.id == ownershipStatusId }
        }
    }
}