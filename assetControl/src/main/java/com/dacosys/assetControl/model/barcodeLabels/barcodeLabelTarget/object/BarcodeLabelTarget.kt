package com.dacosys.assetControl.model.barcodeLabels.barcodeLabelTarget.`object`

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelTarget.dbHelper.BarcodeLabelTargetContract
import java.util.*

class BarcodeLabelTarget : Parcelable {
    var id: Long = 0
    var description: String = ""

    constructor(barcodeLabelTargetId: Long, description: String) {
        this.description = description
        this.id = barcodeLabelTargetId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is BarcodeLabelTarget) {
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

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(BarcodeLabelTargetContract.BarcodeLabelTargetEntry.BARCODE_LABEL_TARGET_ID, id)
        values.put(BarcodeLabelTargetContract.BarcodeLabelTargetEntry.DESCRIPTION, description)
        return values
    }

    companion object CREATOR : Parcelable.Creator<BarcodeLabelTarget> {
        override fun createFromParcel(parcel: Parcel): BarcodeLabelTarget {
            return BarcodeLabelTarget(parcel)
        }

        override fun newArray(size: Int): Array<BarcodeLabelTarget?> {
            return arrayOfNulls(size)
        }

        var None = BarcodeLabelTarget(0, getContext().getString(R.string.none))
        var Asset =
            BarcodeLabelTarget(1, getContext().getString(R.string.asset))
        var WarehouseArea =
            BarcodeLabelTarget(2, getContext().getString(R.string.area))

        fun getAll(): ArrayList<BarcodeLabelTarget> {
            val allSections = ArrayList<BarcodeLabelTarget>()
            Collections.addAll(
                allSections,
                None,
                Asset,
                WarehouseArea
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(barcodeLabelTargetId: Long): BarcodeLabelTarget? {
            return getAll().firstOrNull { it.id == barcodeLabelTargetId }
        }
    }
}