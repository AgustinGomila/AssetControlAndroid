package com.dacosys.assetControl.data.room.entity.location

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea.Entry
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaObject

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"),
        Index(value = [Entry.WAREHOUSE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}")
    ]
)
data class WarehouseArea(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) var mActive: Int = 0,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERRED) var transferred: Int? = null,
    @Ignore var warehouseStr: String = ""
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
        warehouseId = parcel.readLong(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        warehouseStr = parcel.readString().orEmpty()
    )

    constructor(waObj: WarehouseAreaObject) : this(
        id = waObj.warehouse_area_id,
        description = waObj.description,
        mActive = waObj.active,
        warehouseId = waObj.warehouse_id,
        transferred = 0,
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

    companion object CREATOR : Parcelable.Creator<WarehouseArea> {
        override fun createFromParcel(parcel: Parcel): WarehouseArea {
            return WarehouseArea(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseArea?> {
            return arrayOfNulls(size)
        }
    }
}

