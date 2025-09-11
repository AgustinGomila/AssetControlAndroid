package com.example.assetControl.data.enums.common

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R

data class ConfirmStatus(val id: Int, val description: String) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<ConfirmStatus> {
        override fun createFromParcel(parcel: Parcel): ConfirmStatus {
            return ConfirmStatus(parcel)
        }

        override fun newArray(size: Int): Array<ConfirmStatus?> {
            return arrayOfNulls(size)
        }

        var cancel = ConfirmStatus(0, context.getString(R.string.cancel))
        var modify = ConfirmStatus(1, context.getString(R.string.modify))
        var confirm =
            ConfirmStatus(2, context.getString(R.string.confirm))

        fun getAll(): List<ConfirmStatus> {
            return listOf(cancel, modify, confirm)
        }

        fun getById(id: Int): ConfirmStatus? {
            return getAll().firstOrNull { it.id == id }
        }
    }
}

