package com.example.assetControl.data.webservice.movement

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.data.room.dto.movement.WarehouseMovement
import com.example.assetControl.utils.misc.DateUtils.formatDateToString
import org.ksoap2.serialization.SoapObject

class WarehouseMovementObject() : Parcelable {
    var warehouseMovementId: Long = 0
    var warehouseMovementDate: String = ""
    var obs: String = ""
    var userId: Long = 0
    var origWarehouseAreaId: Long = 0
    var origWarehouseId: Long = 0
    var transferedDate: String = ""
    var destWarehouseAreaId: Long = 0
    var destWarehouseId: Long = 0
    var completed: Int = 0
    var collectorWarehouseMovementId: Long = 0

    constructor(parcel: Parcel) : this() {
        warehouseMovementId = parcel.readLong()
        warehouseMovementDate = parcel.readString().orEmpty()
        obs = parcel.readString().orEmpty()
        userId = parcel.readLong()
        origWarehouseAreaId = parcel.readLong()
        origWarehouseId = parcel.readLong()
        transferedDate = parcel.readString().orEmpty()
        destWarehouseAreaId = parcel.readLong()
        destWarehouseId = parcel.readLong()
        completed = parcel.readInt()
        collectorWarehouseMovementId = parcel.readLong()
    }

    constructor(wm: WarehouseMovement) : this() {
        warehouseMovementId = wm.id
        warehouseMovementDate = formatDateToString(wm.warehouseMovementDate)
        obs = wm.obs.orEmpty()
        userId = wm.userId
        origWarehouseAreaId = wm.originWarehouseAreaId
        origWarehouseId = wm.originWarehouseId
        transferedDate = formatDateToString(wm.transferredDate)
        destWarehouseAreaId = wm.destinationWarehouseAreaId
        destWarehouseId = wm.destinationWarehouseId
        completed = wm.completed ?: 0
        collectorWarehouseMovementId = wm.id
    }

    fun getBySoapObject(so: SoapObject): WarehouseMovementObject {
        val x = WarehouseMovementObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "warehouse_movement_id" -> {
                            x.warehouseMovementId = soValue as? Long ?: 0L
                        }

                        "warehouse_movement_date" -> {
                            x.warehouseMovementDate = soValue as? String ?: ""
                        }

                        "obs" -> {
                            x.obs = soValue as? String ?: ""
                        }

                        "user_id" -> {
                            x.userId = soValue as? Long ?: 0L
                        }

                        "origin_warehouse_area_id" -> {
                            x.origWarehouseAreaId = soValue as? Long ?: 0L
                        }

                        "origin_warehouse_id" -> {
                            x.origWarehouseId = soValue as? Long ?: 0L
                        }

                        "destination_warehouse_area_id" -> {
                            x.destWarehouseAreaId = soValue as? Long ?: 0L
                        }

                        "destination_warehouse_id" -> {
                            x.destWarehouseId = soValue as? Long ?: 0L
                        }

                        "transfered_date" -> {
                            x.transferedDate = soValue as? String ?: ""
                        }

                        "completed" -> {
                            x.completed = soValue as? Int ?: 0
                        }

                        "collector_warehouse_movement_id" -> {
                            x.collectorWarehouseMovementId = soValue as? Long ?: 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(warehouseMovementId)
        parcel.writeString(warehouseMovementDate)
        parcel.writeString(obs)
        parcel.writeLong(userId)
        parcel.writeLong(origWarehouseAreaId)
        parcel.writeLong(origWarehouseId)
        parcel.writeString(transferedDate)
        parcel.writeLong(destWarehouseAreaId)
        parcel.writeLong(destWarehouseId)
        parcel.writeInt(completed)
        parcel.writeLong(collectorWarehouseMovementId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseMovementObject> {
        override fun createFromParcel(parcel: Parcel): WarehouseMovementObject {
            return WarehouseMovementObject(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseMovementObject?> {
            return arrayOfNulls(size)
        }
    }
}