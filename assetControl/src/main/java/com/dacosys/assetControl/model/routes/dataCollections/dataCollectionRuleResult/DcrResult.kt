package com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleResult

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import java.util.*

class DcrResult : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(dataCollectionRuleResultId: Int, description: String) {
        this.description = description
        this.id = dataCollectionRuleResultId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is DcrResult) {
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

    companion object CREATOR : Parcelable.Creator<DcrResult> {
        override fun createFromParcel(parcel: Parcel): DcrResult {
            return DcrResult(parcel)
        }

        override fun newArray(size: Int): Array<DcrResult?> {
            return arrayOfNulls(size)
        }

        var cont = DcrResult(0, Statics.AssetControl.getContext().getString(R.string._continue))
        var back = DcrResult(-1, Statics.AssetControl.getContext().getString(R.string.back))
        var end = DcrResult(-2, Statics.AssetControl.getContext().getString(R.string.end))
        var noContinue =
            DcrResult(-3, Statics.AssetControl.getContext().getString(R.string.no_continue))
        var levelX = DcrResult(-4, Statics.AssetControl.getContext().getString(R.string.level_x))

        fun getAll(): ArrayList<DcrResult> {
            val allSections = ArrayList<DcrResult>()
            Collections.addAll(
                allSections,
                cont,
                back,
                end,
                noContinue,
                levelX
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(dataCollectionRuleResultId: Int): DcrResult? {
            return getAll().firstOrNull { it.id == dataCollectionRuleResultId }
        }
    }
}