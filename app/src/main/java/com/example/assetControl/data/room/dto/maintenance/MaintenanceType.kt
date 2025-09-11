package com.example.assetControl.data.room.dto.maintenance

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo

abstract class MaintenanceTypeEntry {
    companion object {
        const val TABLE_NAME = "maintenance_type"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val MAINTENANCE_TYPE_GROUP_ID = "maintenance_type_group_id"
    }
}

class MaintenanceType(
    @ColumnInfo(name = MaintenanceTypeEntry.ID) val id: Long,
    @ColumnInfo(name = MaintenanceTypeEntry.DESCRIPTION) val description: String,
    @ColumnInfo(name = MaintenanceTypeEntry.ACTIVE) val active: Int,
    @ColumnInfo(name = MaintenanceTypeEntry.MAINTENANCE_TYPE_GROUP_ID) val maintenanceTypeGroupId: Long
) : Parcelable {

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MaintenanceType

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        active = parcel.readInt(),
        maintenanceTypeGroupId = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
        parcel.writeLong(maintenanceTypeGroupId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MaintenanceType> {
        override fun createFromParcel(parcel: Parcel): MaintenanceType {
            return MaintenanceType(parcel)
        }

        override fun newArray(size: Int): Array<MaintenanceType?> {
            return arrayOfNulls(size)
        }
    }
}

