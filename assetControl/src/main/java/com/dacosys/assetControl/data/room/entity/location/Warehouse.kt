package com.dacosys.assetControl.data.room.entity.location

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.location.Warehouse.Entry
import com.dacosys.assetControl.data.webservice.location.WarehouseObject

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"),
    ]
)
data class Warehouse(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) var mActive: Int = 0,
    @ColumnInfo(name = Entry.TRANSFERRED) val transferred: Int? = null
) : Parcelable {

    @Ignore
    var active: Boolean = mActive == 1
        set(value) {
            mActive = if (value) 1 else 0
            field = value
        }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        mActive = parcel.readInt(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int
    )

    constructor(wObj: WarehouseObject) : this(
        id = wObj.warehouse_id,
        description = wObj.description,
        mActive = wObj.active,
        transferred = 0
    )

    object Entry {
        const val TABLE_NAME = "warehouse"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val TRANSFERRED = "transferred"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(mActive)
        parcel.writeValue(transferred)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Warehouse> {
        override fun createFromParcel(parcel: Parcel): Warehouse {
            return Warehouse(parcel)
        }

        override fun newArray(size: Int): Array<Warehouse?> {
            return arrayOfNulls(size)
        }
    }
}