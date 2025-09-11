package com.example.assetControl.data.enums.asset

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R

data class AssetCondition(val id: Int, val description: String) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<AssetCondition> {
        override fun createFromParcel(parcel: Parcel): AssetCondition {
            return AssetCondition(parcel)
        }

        override fun newArray(size: Int): Array<AssetCondition?> {
            return arrayOfNulls(size)
        }

        var unknown = AssetCondition(0, context.getString(R.string.asset_condition_unknown))
        var excellent = AssetCondition(1, context.getString(R.string.asset_condition_excellent))
        var veryGood = AssetCondition(2, context.getString(R.string.asset_condition_very_good))
        var good = AssetCondition(3, context.getString(R.string.asset_condition_good))
        var regular = AssetCondition(4, context.getString(R.string.asset_condition_regular))
        var bad = AssetCondition(5, context.getString(R.string.asset_condition_bad))
        var veryBad = AssetCondition(6, context.getString(R.string.asset_condition_very_bad))

        fun getAll(): List<AssetCondition> {
            return listOf(
                unknown,
                excellent,
                veryGood,
                good,
                regular,
                bad,
                veryBad
            )
        }

        fun getById(id: Int): AssetCondition {
            return getAll().firstOrNull { it.id == id } ?: unknown
        }
    }
}




