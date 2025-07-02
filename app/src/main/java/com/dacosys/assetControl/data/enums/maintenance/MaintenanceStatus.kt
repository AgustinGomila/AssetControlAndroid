package com.dacosys.assetControl.data.enums.maintenance

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.R

data class MaintenanceStatus(val id: Int, val description: String) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<MaintenanceStatus> {
        override fun createFromParcel(parcel: Parcel): MaintenanceStatus {
            return MaintenanceStatus(parcel)
        }

        override fun newArray(size: Int): Array<MaintenanceStatus?> {
            return arrayOfNulls(size)
        }

        var unknown = MaintenanceStatus(
            0, context.getString(R.string.unknown)
        )
        var repair = MaintenanceStatus(
            1, context.getString(R.string.maintenance_status_repair)
        )
        var income = MaintenanceStatus(
            2, context.getString(R.string.maintenance_status_income)
        )
        var underDiagnosis = MaintenanceStatus(
            3, context.getString(R.string.maintenance_status_under_diagnosis)
        )
        var diagnosed = MaintenanceStatus(
            4, context.getString(R.string.maintenance_status_diagnosed)
        )
        var cost = MaintenanceStatus(
            5, context.getString(R.string.maintenance_status_cost)
        )
        var approvedCost = MaintenanceStatus(
            6, context.getString(R.string.maintenance_status_approved_cost)
        )
        var underRepair = MaintenanceStatus(
            7, context.getString(R.string.maintenance_status_under_repair)
        )
        var repaired = MaintenanceStatus(
            8, context.getString(R.string.maintenance_status_repaired)
        )
        var finished = MaintenanceStatus(
            9, context.getString(R.string.maintenance_status_finished)
        )
        var repairImposible = MaintenanceStatus(
            10, context.getString(R.string.maintenance_status_repair_imposible)
        )

        fun getAll(): List<MaintenanceStatus> {
            return listOf(
                unknown,
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
        }

        fun getById(id: Int): MaintenanceStatus? {
            return getAll().firstOrNull { it.id == id }
        }
    }
}

