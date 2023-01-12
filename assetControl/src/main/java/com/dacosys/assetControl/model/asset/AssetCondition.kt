package com.dacosys.assetControl.model.asset

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import java.util.*

class AssetCondition : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(assetConditionId: Int, description: String) {
        this.description = description
        this.id = assetConditionId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AssetCondition) {
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

    companion object CREATOR : Parcelable.Creator<AssetCondition> {
        override fun createFromParcel(parcel: Parcel): AssetCondition {
            return AssetCondition(parcel)
        }

        override fun newArray(size: Int): Array<AssetCondition?> {
            return arrayOfNulls(size)
        }

        var unknown = AssetCondition(
            0,
            getContext().getString(R.string.asset_condition_unknown)
        )
        var excellent = AssetCondition(
            1,
            getContext().getString(R.string.asset_condition_excellent)
        )
        var veryGood = AssetCondition(
            2,
            getContext().getString(R.string.asset_condition_very_good)
        )
        var good = AssetCondition(
            3,
            getContext().getString(R.string.asset_condition_good)
        )
        var regular = AssetCondition(
            4,
            getContext().getString(R.string.asset_condition_regular)
        )
        var bad = AssetCondition(
            5,
            getContext().getString(R.string.asset_condition_bad)
        )
        var varyBad = AssetCondition(
            6,
            getContext().getString(R.string.asset_condition_very_bad)
        )

        fun getAll(): ArrayList<AssetCondition> {
            val allSections = ArrayList<AssetCondition>()
            Collections.addAll(
                allSections,
                unknown,
                excellent,
                veryGood,
                good,
                regular,
                bad,
                varyBad
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(assetConditionId: Int): AssetCondition? {
            return getAll().firstOrNull { it.id == assetConditionId }
        }
    }
}