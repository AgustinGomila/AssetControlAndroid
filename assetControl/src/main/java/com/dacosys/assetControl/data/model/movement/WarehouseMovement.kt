package com.dacosys.assetControl.data.model.movement

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.COLLECTOR_WAREHOUSE_MOVEMENT_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.COMPLETED
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.DESTINATION_WAREHOUSE_AREA_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.DESTINATION_WAREHOUSE_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.OBS
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.ORIGIN_WAREHOUSE_AREA_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.ORIGIN_WAREHOUSE_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.TRANSFERED_DATE
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.USER_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.WAREHOUSE_MOVEMENT_DATE
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementContract.WarehouseMovementEntry.Companion.WAREHOUSE_MOVEMENT_ID
import com.dacosys.assetControl.data.dataBase.movement.WarehouseMovementDbHelper
import com.dacosys.assetControl.data.model.location.Warehouse
import com.dacosys.assetControl.data.model.location.WarehouseArea
import com.dacosys.assetControl.data.model.user.User

class WarehouseMovement : Parcelable {
    var collectorWarehouseMovementId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        warehouseMovementId: Long,
        warehouseMovementDate: String,
        obs: String,
        userId: Long,
        origWarehouseAreaId: Long,
        origWarehouseId: Long,
        transferredDate: String?,
        destWarehouseAreaId: Long,
        destWarehouseId: Long,
        completed: Boolean,
        collectorWarehouseMovementId: Long,
    ) {
        this.warehouseMovementId = warehouseMovementId
        this.warehouseMovementDate = warehouseMovementDate
        this.obs = obs
        this.userId = userId
        this.origWarehouseAreaId = origWarehouseAreaId
        this.origWarehouseId = origWarehouseId
        this.transferedDate = transferredDate
        this.destWarehouseAreaId = destWarehouseAreaId
        this.destWarehouseId = destWarehouseId
        this.completed = completed
        this.collectorWarehouseMovementId = collectorWarehouseMovementId

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        collectorWarehouseMovementId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp = WarehouseMovementDbHelper().selectById(this.collectorWarehouseMovementId)

        dataRead = true
        return when {
            temp != null -> {
                this.warehouseMovementId = temp.warehouseMovementId
                this.warehouseMovementDate = temp.warehouseMovementDate
                this.obs = temp.obs
                this.userId = temp.userId
                this.origWarehouseAreaId = temp.origWarehouseAreaId
                this.origWarehouseId = temp.origWarehouseId
                this.transferedDate = temp.transferedDate
                this.destWarehouseAreaId = temp.destWarehouseAreaId
                this.destWarehouseId = temp.destWarehouseId
                this.completed = temp.completed
                this.collectorWarehouseMovementId = temp.collectorWarehouseMovementId

                this.origWarehouseAreaStr = temp.origWarehouseAreaStr
                this.destWarehouseAreaStr = temp.destWarehouseAreaStr
                this.origWarehouseStr = temp.origWarehouseStr
                this.destWarehouseStr = temp.destWarehouseStr

                true
            }

            else -> false
        }
    }

    var warehouseMovementDate: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var transferedDate: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    var obs: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    private var warehouseMovementId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    val user: User?
        get() {
            return if (userId == 0L) null
            else User(userId, false)
        }

    var userId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    val origWarehouseArea: WarehouseArea?
        get() {
            return if (origWarehouseAreaId == 0L) null
            else WarehouseArea(origWarehouseAreaId, false)
        }

    var origWarehouseAreaId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var origWarehouseAreaStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    val origWarehouse: Warehouse?
        get() {
            return if (origWarehouseId == 0L) null
            else Warehouse(origWarehouseId, false)
        }

    var origWarehouseId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var origWarehouseStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    val destWarehouseArea: WarehouseArea?
        get() {
            return if (destWarehouseAreaId == 0L) null
            else WarehouseArea(destWarehouseAreaId, false)
        }

    var destWarehouseAreaId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var destWarehouseAreaStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    val destWarehouse: Warehouse?
        get() {
            return if (destWarehouseId == 0L) null
            else Warehouse(destWarehouseId, false)
        }

    var destWarehouseId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var destWarehouseStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var completed: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    constructor(parcel: android.os.Parcel) {
        warehouseMovementId = parcel.readLong()
        warehouseMovementDate = parcel.readString() ?: ""
        obs = parcel.readString() ?: ""
        userId = parcel.readLong()
        origWarehouseAreaId = parcel.readLong()
        origWarehouseId = parcel.readLong()
        transferedDate = parcel.readString()
        destWarehouseAreaId = parcel.readLong()
        destWarehouseId = parcel.readLong()
        completed = parcel.readByte() != 0.toByte()
        collectorWarehouseMovementId = parcel.readLong()

        origWarehouseStr = parcel.readString() ?: ""
        origWarehouseAreaStr = parcel.readString() ?: ""
        destWarehouseStr = parcel.readString() ?: ""
        destWarehouseAreaStr = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(WAREHOUSE_MOVEMENT_ID, warehouseMovementId)
        values.put(WAREHOUSE_MOVEMENT_DATE, warehouseMovementDate)
        values.put(OBS, obs)
        values.put(USER_ID, userId)
        values.put(ORIGIN_WAREHOUSE_AREA_ID, origWarehouseAreaId)
        values.put(ORIGIN_WAREHOUSE_ID, origWarehouseId)
        values.put(DESTINATION_WAREHOUSE_AREA_ID, destWarehouseAreaId)
        values.put(DESTINATION_WAREHOUSE_ID, destWarehouseId)
        values.put(TRANSFERED_DATE, transferedDate)
        values.put(COMPLETED, completed)
        values.put(COLLECTOR_WAREHOUSE_MOVEMENT_ID, collectorWarehouseMovementId)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return WarehouseMovementDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is WarehouseMovement) {
            false
        } else equals(this.collectorWarehouseMovementId, other.collectorWarehouseMovementId)
    }

    override fun hashCode(): Int {
        return this.collectorWarehouseMovementId.hashCode()
    }

    class CustomComparator : Comparator<WarehouseMovement> {
        override fun compare(o1: WarehouseMovement, o2: WarehouseMovement): Int {
            if (o1.collectorWarehouseMovementId < o2.collectorWarehouseMovementId) {
                return -1
            } else if (o1.collectorWarehouseMovementId > o2.collectorWarehouseMovementId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(warehouseMovementId)
        parcel.writeString(warehouseMovementDate)
        parcel.writeString(obs)
        parcel.writeLong(userId)
        parcel.writeLong(origWarehouseAreaId)
        parcel.writeLong(origWarehouseId)
        parcel.writeString(transferedDate)
        parcel.writeLong(destWarehouseAreaId)
        parcel.writeLong(destWarehouseId)
        parcel.writeByte(if (completed) 1 else 0)
        parcel.writeLong(collectorWarehouseMovementId)

        parcel.writeString(origWarehouseStr)
        parcel.writeString(origWarehouseAreaStr)
        parcel.writeString(destWarehouseStr)
        parcel.writeString(destWarehouseAreaStr)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseMovement> {
        override fun createFromParcel(parcel: android.os.Parcel): WarehouseMovement {
            return WarehouseMovement(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseMovement?> {
            return arrayOfNulls(size)
        }
    }
}