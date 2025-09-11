package com.example.assetControl.data.enums.barcode

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R

data class BarcodeLabelTarget(val id: Int, val description: String) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<BarcodeLabelTarget> {
        override fun createFromParcel(parcel: Parcel): BarcodeLabelTarget {
            return BarcodeLabelTarget(parcel)
        }

        override fun newArray(size: Int): Array<BarcodeLabelTarget?> {
            return arrayOfNulls(size)
        }

        var None = BarcodeLabelTarget(0, context.getString(R.string.none))
        var Asset =
            BarcodeLabelTarget(1, context.getString(R.string.asset))
        var WarehouseArea =
            BarcodeLabelTarget(2, context.getString(R.string.area))

        fun getAll(): List<BarcodeLabelTarget> {
            return listOf(
                None,
                Asset,
                WarehouseArea
            )
        }

        fun getById(id: Int): BarcodeLabelTarget {
            return getAll().firstOrNull { it.id == id } ?: None
        }
    }
}




