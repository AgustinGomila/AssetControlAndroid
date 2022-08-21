package com.dacosys.assetControl.model.assets.manteinances.manteinanceStatus.`object`

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.model.assets.manteinances.manteinanceStatus.dbHelper.ManteinanceStatusContract
import java.util.*

class ManteinanceStatus : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(manteinanceStatusId: Int, description: String) {
        this.description = description
        this.id = manteinanceStatusId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is ManteinanceStatus) {
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

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(ManteinanceStatusContract.ManteinanceStatusEntry.MANTEINANCE_STATUS_ID, id)
        values.put(ManteinanceStatusContract.ManteinanceStatusEntry.DESCRIPTION, description)
        return values
    }

    companion object CREATOR : Parcelable.Creator<ManteinanceStatus> {
        override fun createFromParcel(parcel: Parcel): ManteinanceStatus {
            return ManteinanceStatus(parcel)
        }

        override fun newArray(size: Int): Array<ManteinanceStatus?> {
            return arrayOfNulls(size)
        }

        var repair = ManteinanceStatus(
            1,
            Statics.AssetControl.getContext().getString(R.string.maintenance_status_repair)
        )
        var income = ManteinanceStatus(
            2,
            Statics.AssetControl.getContext().getString(R.string.maintenance_status_income)
        )
        var underDiagnosis =
            ManteinanceStatus(
                3,
                Statics.AssetControl.getContext()
                    .getString(R.string.maintenance_status_under_diagnosis)
            )
        var diagnosed = ManteinanceStatus(
            4,
            Statics.AssetControl.getContext().getString(R.string.maintenance_status_diagnosed)
        )
        var cost = ManteinanceStatus(
            5,
            Statics.AssetControl.getContext().getString(R.string.maintenance_status_cost)
        )
        var approvedCost =
            ManteinanceStatus(
                6,
                Statics.AssetControl.getContext()
                    .getString(R.string.maintenance_status_approved_cost)
            )
        var underRepair = ManteinanceStatus(
            7,
            Statics.AssetControl.getContext().getString(R.string.maintenance_status_under_repair)
        )
        var repaired = ManteinanceStatus(
            8,
            Statics.AssetControl.getContext().getString(R.string.maintenance_status_repaired)
        )
        var finished = ManteinanceStatus(
            9,
            Statics.AssetControl.getContext().getString(R.string.maintenance_status_finished)
        )
        var repairImposible =
            ManteinanceStatus(
                10,
                Statics.AssetControl.getContext()
                    .getString(R.string.maintenance_status_repair_imposible)
            )

        fun getAll(): ArrayList<ManteinanceStatus> {
            val allSections = ArrayList<ManteinanceStatus>()
            Collections.addAll(
                allSections,
                repair,
                income,
                underDiagnosis,
                diagnosed,
                cost,
                approvedCost,
                underRepair,
                repaired,
                finished,
                repairImposible
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(manteinanceStatusId: Int): ManteinanceStatus? {
            return getAll().firstOrNull { it.id == manteinanceStatusId }
        }
    }
}