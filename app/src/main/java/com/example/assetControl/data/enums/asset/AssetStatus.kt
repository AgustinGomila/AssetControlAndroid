package com.example.assetControl.data.enums.asset

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R

data class AssetStatus(val id: Int, val description: String) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<AssetStatus> {
        override fun createFromParcel(parcel: Parcel): AssetStatus {
            return AssetStatus(parcel)
        }

        override fun newArray(size: Int): Array<AssetStatus?> {
            return arrayOfNulls(size)
        }

        val unknown = AssetStatus(0, context.getString(R.string.asset_status_unknown))
        var onInventory = AssetStatus(1, context.getString(R.string.asset_status_on_inventory))
        var removed = AssetStatus(2, context.getString(R.string.asset_status_removed))
        var missing = AssetStatus(3, context.getString(R.string.asset_status_missing))

        fun getAll(): List<AssetStatus> {
            return listOf(unknown, onInventory, removed, missing)
        }

        fun getById(id: Int): AssetStatus {
            return getAll().firstOrNull { it.id == id } ?: unknown
        }
    }
}

