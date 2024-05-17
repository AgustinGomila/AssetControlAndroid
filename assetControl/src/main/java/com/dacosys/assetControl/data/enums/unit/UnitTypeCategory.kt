package com.dacosys.assetControl.data.enums.unit

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R

data class UnitTypeCategory(val id: Int, val description: String) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<UnitTypeCategory> {
        override fun createFromParcel(parcel: Parcel): UnitTypeCategory {
            return UnitTypeCategory(parcel)
        }

        override fun newArray(size: Int): Array<UnitTypeCategory?> {
            return arrayOfNulls(size)
        }

        var unknown = UnitTypeCategory(0, getContext().getString(R.string.unknown))
        var temperature = UnitTypeCategory(1, getContext().getString(R.string.temperature))
        var weight = UnitTypeCategory(2, getContext().getString(R.string.weight))
        var length = UnitTypeCategory(3, getContext().getString(R.string.length))
        var volume = UnitTypeCategory(4, getContext().getString(R.string.volume))
        var quantity = UnitTypeCategory(5, getContext().getString(R.string.quantity))
        var area = UnitTypeCategory(6, getContext().getString(R.string.area_surface))
        var pressure = UnitTypeCategory(7, getContext().getString(R.string.pressure))

        fun getAll(): List<UnitTypeCategory> {
            return listOf(unknown, temperature, weight, length, volume, quantity, area, pressure)
        }

        fun getById(id: Int): UnitTypeCategory {
            return getAll().firstOrNull { it.id == id } ?: unknown
        }
    }
}