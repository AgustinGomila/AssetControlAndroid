package com.dacosys.assetControl.model.asset

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import java.util.*

class AssetStatus : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(assetStatusId: Int, description: String) {
        this.description = description
        this.id = assetStatusId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AssetStatus) {
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

    companion object CREATOR : Parcelable.Creator<AssetStatus> {
        override fun createFromParcel(parcel: Parcel): AssetStatus {
            return AssetStatus(parcel)
        }

        override fun newArray(size: Int): Array<AssetStatus?> {
            return arrayOfNulls(size)
        }

        var unknown = AssetStatus(
            0,
            getContext().getString(R.string.asset_status_unknown)
        )
        var onInventory = AssetStatus(
            1,
            getContext().getString(R.string.asset_status_on_inventory)
        )
        var removed = AssetStatus(
            2,
            getContext().getString(R.string.asset_status_removed)
        )
        var missing = AssetStatus(
            3,
            getContext().getString(R.string.asset_status_missing)
        )

        fun getAll(): ArrayList<AssetStatus> {
            val allSections = ArrayList<AssetStatus>()
            Collections.addAll(
                allSections,
                unknown,
                onInventory,
                removed,
                missing
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getAllIdAsString(): ArrayList<String> {
            val allSections = ArrayList<String>()
            Collections.addAll(
                allSections,
                unknown.id.toString(),
                onInventory.id.toString(),
                removed.id.toString(),
                unknown.id.toString()
            )

            return ArrayList(allSections.sortedWith(compareBy { it }))
        }

        fun getById(assetStatusId: Int): AssetStatus? {
            return getAll().firstOrNull { it.id == assetStatusId }
        }
    }
}