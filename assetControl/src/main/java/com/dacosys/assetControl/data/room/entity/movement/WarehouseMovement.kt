package com.dacosys.assetControl.data.room.entity.movement

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement.Entry
import com.dacosys.assetControl.data.room.repository.movement.WarehouseMovementRepository
import com.dacosys.assetControl.utils.Statics.Companion.toDate
import java.util.*

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.WAREHOUSE_MOVEMENT_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_MOVEMENT_ID}"),
        Index(value = [Entry.USER_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"),
        Index(
            value = [Entry.ORIGIN_WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ORIGIN_WAREHOUSE_AREA_ID}"
        ),
        Index(value = [Entry.ORIGIN_WAREHOUSE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ORIGIN_WAREHOUSE_ID}"),
        Index(
            value = [Entry.DESTINATION_WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESTINATION_WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [Entry.DESTINATION_WAREHOUSE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESTINATION_WAREHOUSE_ID}"
        )
    ]
)
data class WarehouseMovement(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_ID) var warehouseMovementId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_DATE) var warehouseMovementDate: Date = Date(),
    @ColumnInfo(name = Entry.OBS) var obs: String? = null,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_AREA_ID) var originWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_ID) var originWarehouseId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERED_DATE) var transferedDate: Date? = null,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_AREA_ID) var destinationWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_ID) var destinationWarehouseId: Long = 0L,
    @ColumnInfo(name = Entry.COMPLETED) var completed: Int? = null,
    @Ignore var origWarehouseAreaStr: String = "",
    @Ignore var destWarehouseAreaStr: String = "",
) : Parcelable {

    fun saveChanges() = WarehouseMovementRepository().update(this)

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        warehouseMovementId = parcel.readLong(),
        warehouseMovementDate = parcel.readString().orEmpty().toDate(),
        obs = parcel.readString(),
        userId = parcel.readLong(),
        originWarehouseAreaId = parcel.readLong(),
        originWarehouseId = parcel.readLong(),
        transferedDate = parcel.readString().orEmpty().toDate(),
        destinationWarehouseAreaId = parcel.readLong(),
        destinationWarehouseId = parcel.readLong(),
        completed = parcel.readValue(Int::class.java.classLoader) as? Int,
        origWarehouseAreaStr = parcel.readString().orEmpty(),
        destWarehouseAreaStr = parcel.readString().orEmpty()
    )

    object Entry {
        const val TABLE_NAME = "warehouse_movement"
        const val ID = "_id"
        const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
        const val WAREHOUSE_MOVEMENT_DATE = "warehouse_movement_date"
        const val OBS = "obs"
        const val USER_ID = "user_id"
        const val ORIGIN_WAREHOUSE_AREA_ID = "origin_warehouse_area_id"
        const val ORIGIN_WAREHOUSE_ID = "origin_warehouse_id"
        const val TRANSFERED_DATE = "transfered_date"
        const val DESTINATION_WAREHOUSE_AREA_ID = "destination_warehouse_area_id"
        const val DESTINATION_WAREHOUSE_ID = "destination_warehouse_id"
        const val COMPLETED = "completed"

        const val ORIGIN_WAREHOUSE_AREA_STR = "origin_warehouse_area_str"
        const val ORIGIN_WAREHOUSE_STR = "origin_warehouse_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(warehouseMovementId)
        parcel.writeString(obs)
        parcel.writeLong(userId)
        parcel.writeLong(originWarehouseAreaId)
        parcel.writeLong(originWarehouseId)
        parcel.writeLong(destinationWarehouseAreaId)
        parcel.writeLong(destinationWarehouseId)
        parcel.writeValue(completed)
        parcel.writeString(origWarehouseAreaStr)
        parcel.writeString(destWarehouseAreaStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseMovement> {
        override fun createFromParcel(parcel: Parcel): WarehouseMovement {
            return WarehouseMovement(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseMovement?> {
            return arrayOfNulls(size)
        }
    }
}
