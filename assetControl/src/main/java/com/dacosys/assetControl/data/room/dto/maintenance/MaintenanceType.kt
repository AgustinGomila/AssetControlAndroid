package com.dacosys.assetControl.data.room.dto.maintenance

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo

class MaintenanceType(
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.MAINTENANCE_TYPE_GROUP_ID) val maintenanceTypeGroupId: Long
) : Parcelable {

    override fun toString(): String {
        return description
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        active = parcel.readInt(),
        maintenanceTypeGroupId = parcel.readLong()
    )

    object Entry {
        const val TABLE_NAME = "maintenance_type"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val MAINTENANCE_TYPE_GROUP_ID = "maintenance_type_group_id"
    }

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

