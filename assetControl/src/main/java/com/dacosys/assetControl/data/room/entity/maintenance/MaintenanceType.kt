package com.dacosys.assetControl.data.room.entity.maintenance

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceType.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.MANTEINANCE_TYPE_GROUP_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.MANTEINANCE_TYPE_GROUP_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class MaintenanceType(
    @PrimaryKey @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.MANTEINANCE_TYPE_GROUP_ID) val manteinanceTypeGroupId: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        active = parcel.readInt(),
        manteinanceTypeGroupId = parcel.readLong()
    )

    object Entry {
        const val TABLE_NAME = "manteinance_type"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val MANTEINANCE_TYPE_GROUP_ID = "manteinance_type_group_id"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
        parcel.writeLong(manteinanceTypeGroupId)
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

