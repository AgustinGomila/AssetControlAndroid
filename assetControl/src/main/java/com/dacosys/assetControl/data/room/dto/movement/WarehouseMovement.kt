package com.dacosys.assetControl.data.room.dto.movement

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.room.repository.movement.WarehouseMovementRepository
import com.dacosys.assetControl.utils.Statics.Companion.toDate
import java.util.*

class WarehouseMovement(
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_ID) var warehouseMovementId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_DATE) var warehouseMovementDate: Date = Date(),
    @ColumnInfo(name = Entry.OBS) var obs: String? = null,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_AREA_ID) var originWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_ID) var originWarehouseId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) var transferredDate: Date? = null,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_AREA_ID) var destinationWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_ID) var destinationWarehouseId: Long = 0L,
    @ColumnInfo(name = Entry.COMPLETED) var completed: Int? = null,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_STR) var origWarehouseStr: String? = null,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_AREA_STR) var origWarehouseAreaStr: String? = null,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_STR) var destWarehouseStr: String? = null,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_AREA_STR) var destWarehouseAreaStr: String? = null,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WarehouseMovement

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun saveChanges() = WarehouseMovementRepository().update(this)

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        warehouseMovementId = parcel.readLong(),
        warehouseMovementDate = parcel.readString().orEmpty().toDate(),
        obs = parcel.readString(),
        userId = parcel.readLong(),
        originWarehouseAreaId = parcel.readLong(),
        originWarehouseId = parcel.readLong(),
        transferredDate = parcel.readString().orEmpty().toDate(),
        destinationWarehouseAreaId = parcel.readLong(),
        destinationWarehouseId = parcel.readLong(),
        completed = parcel.readValue(Int::class.java.classLoader) as? Int,
        origWarehouseStr = parcel.readString().orEmpty(),
        origWarehouseAreaStr = parcel.readString().orEmpty(),
        destWarehouseStr = parcel.readString().orEmpty(),
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
        const val TRANSFERRED_DATE = "transferred_date"
        const val DESTINATION_WAREHOUSE_AREA_ID = "destination_warehouse_area_id"
        const val DESTINATION_WAREHOUSE_ID = "destination_warehouse_id"
        const val COMPLETED = "completed"

        const val ORIGIN_WAREHOUSE_AREA_STR = "origin_warehouse_area_str"
        const val ORIGIN_WAREHOUSE_STR = "origin_warehouse_str"
        const val DESTINATION_WAREHOUSE_AREA_STR = "destination_warehouse_area_str"
        const val DESTINATION_WAREHOUSE_STR = "destination_warehouse_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(warehouseMovementId)
        parcel.writeString(warehouseMovementDate.toString())
        parcel.writeString(obs)
        parcel.writeLong(userId)
        parcel.writeLong(originWarehouseAreaId)
        parcel.writeLong(originWarehouseId)
        parcel.writeString(transferredDate?.toString())
        parcel.writeLong(destinationWarehouseAreaId)
        parcel.writeLong(destinationWarehouseId)
        parcel.writeValue(completed)
        parcel.writeString(origWarehouseStr)
        parcel.writeString(origWarehouseAreaStr)
        parcel.writeString(destWarehouseStr)
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
