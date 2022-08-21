package com.dacosys.assetControl.model.assets.units.unitTypeCategory

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import java.util.*

class UnitTypeCategory : Parcelable {
    var id: Long = 0
    var description: String = ""

    constructor(unitTypeCategoryId: Long, description: String) {
        this.description = description
        this.id = unitTypeCategoryId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is UnitTypeCategory) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    constructor(parcel: Parcel) {
        id = parcel.readLong()
        description = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
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

        var unknown = UnitTypeCategory(
            0, Statics.AssetControl.getContext().getString(
                R.string.unknown
            )
        )
        var temperature =
            UnitTypeCategory(1, Statics.AssetControl.getContext().getString(R.string.temperature))
        var weight =
            UnitTypeCategory(2, Statics.AssetControl.getContext().getString(R.string.weight))
        var lenght =
            UnitTypeCategory(3, Statics.AssetControl.getContext().getString(R.string.lenght))
        var volume =
            UnitTypeCategory(4, Statics.AssetControl.getContext().getString(R.string.volume))
        var quantity =
            UnitTypeCategory(5, Statics.AssetControl.getContext().getString(R.string.quantity))
        var area =
            UnitTypeCategory(6, Statics.AssetControl.getContext().getString(R.string.area_surface))
        var pressure =
            UnitTypeCategory(7, Statics.AssetControl.getContext().getString(R.string.pressure))

        fun getAll(): ArrayList<UnitTypeCategory> {
            val allSections = ArrayList<UnitTypeCategory>()
            Collections.addAll(
                allSections,
                unknown,
                temperature,
                weight,
                lenght,
                volume,
                quantity,
                area,
                pressure
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(unitTypeCategoryId: Long): UnitTypeCategory? {
            return getAll().firstOrNull { it.id == unitTypeCategoryId }
        }
    }
}