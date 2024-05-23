package com.dacosys.assetControl.data.room.dto.location

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaObject

class WarehouseArea(
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) var mActive: Int = 0,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERRED) var transferred: Int? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_STR) var warehouseStr: String = ""
) : Parcelable {

    override fun toString(): String {
        return description
    }

    @Ignore
    var active: Boolean = mActive == 1
        get() = mActive == 1
        set(value) {
            mActive = if (value) 1 else 0
            field = value
        }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        mActive = parcel.readInt(),
        warehouseId = parcel.readLong(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        warehouseStr = parcel.readString().orEmpty()
    )

    constructor(waObj: WarehouseAreaObject) : this(
        id = waObj.warehouse_area_id,
        description = waObj.description,
        mActive = waObj.active,
        warehouseId = waObj.warehouse_id,
        transferred = 1,
    )

    object Entry {
        const val TABLE_NAME = "warehouse_area"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val WAREHOUSE_ID = "warehouse_id"
        const val TRANSFERRED = "transferred"

        const val WAREHOUSE_STR = "warehouse_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(mActive)
        parcel.writeLong(warehouseId)
        parcel.writeValue(transferred)
        parcel.writeString(warehouseStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WarehouseArea

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<WarehouseArea> {
        override fun createFromParcel(parcel: Parcel): WarehouseArea {
            return WarehouseArea(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseArea?> {
            return arrayOfNulls(size)
        }
    }
}

