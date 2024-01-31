package com.dacosys.assetControl.data.model.movement

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import java.util.*

class WarehouseMovementContentStatus : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(wmContStatusId: Int, description: String) {
        this.description = description
        this.id = wmContStatusId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is WarehouseMovementContentStatus) {
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

    companion object CREATOR : Parcelable.Creator<WarehouseMovementContentStatus> {
        override fun createFromParcel(parcel: Parcel): WarehouseMovementContentStatus {
            return WarehouseMovementContentStatus(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseMovementContentStatus?> {
            return arrayOfNulls(size)
        }

        var toMove = WarehouseMovementContentStatus(
            wmContStatusId = 0,
            description = getContext().getString(R.string.to_move)
        )
        var noNeedToMove = WarehouseMovementContentStatus(
            wmContStatusId = 3,
            description = getContext().getString(R.string.no_need_to_move)
        )

        fun getAll(): ArrayList<WarehouseMovementContentStatus> {
            val allSections = ArrayList<WarehouseMovementContentStatus>()
            Collections.addAll(
                allSections,
                toMove,
                noNeedToMove
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(wmContStatusId: Int): WarehouseMovementContentStatus? {
            return getAll().firstOrNull { it.id == wmContStatusId }
        }
    }
}