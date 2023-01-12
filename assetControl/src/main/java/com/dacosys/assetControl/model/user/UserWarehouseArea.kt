package com.dacosys.assetControl.model.user

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.dataBase.user.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.CHECK
import com.dacosys.assetControl.dataBase.user.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.COUNT
import com.dacosys.assetControl.dataBase.user.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.MOVE
import com.dacosys.assetControl.dataBase.user.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.SEE
import com.dacosys.assetControl.dataBase.user.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.USER_ID
import com.dacosys.assetControl.dataBase.user.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.dataBase.user.UserWarehouseAreaDbHelper

class UserWarehouseArea : Parcelable {
    constructor(
        userId: Long,
        warehouseAreaId: Long,
        see: Boolean,
        move: Boolean,
        count: Boolean,
        check: Boolean,
    ) {
        this.userId = userId
        this.warehouseAreaId = warehouseAreaId
        this.see = see
        this.check = check
        this.count = count
        this.move = move
    }

    override fun toString(): String {
        return "$userId,$warehouseAreaId"
    }

    var userId: Long = 0
    var warehouseAreaId: Long = 0
    var see: Boolean = false
    private var move: Boolean = false
    var count: Boolean = false
    var check: Boolean = false

    constructor(parcel: Parcel) {
        userId = parcel.readLong()
        warehouseAreaId = parcel.readLong()
        see = parcel.readByte() != 0.toByte()
        check = parcel.readByte() != 0.toByte()
        move = parcel.readByte() != 0.toByte()
        count = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(USER_ID, userId)
        values.put(WAREHOUSE_AREA_ID, warehouseAreaId)
        values.put(SEE, see)
        values.put(MOVE, move)
        values.put(COUNT, count)
        values.put(CHECK, check)
        return values
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is UserWarehouseArea) {
            false
        } else this.userId == other.userId && this.warehouseAreaId == other.warehouseAreaId
    }

    override fun hashCode(): Int {
        return this.userId.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(userId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeByte(if (see) 1 else 0)
        parcel.writeByte(if (move) 1 else 0)
        parcel.writeByte(if (check) 1 else 0)
        parcel.writeByte(if (count) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserWarehouseArea> {
        override fun createFromParcel(parcel: Parcel): UserWarehouseArea {
            return UserWarehouseArea(parcel)
        }

        override fun newArray(size: Int): Array<UserWarehouseArea?> {
            return arrayOfNulls(size)
        }

        fun add(
            userId: Long,
            warehouseAreaId: Long,
            see: Boolean,
            count: Boolean,
            move: Boolean,
            check: Boolean,
        ): Boolean {
            if (userId < 1 || warehouseAreaId < 1) {
                return false
            }

            val i = UserWarehouseAreaDbHelper()
            return i.insert(userId, warehouseAreaId, see, move, count, check)
        }

        fun equals(a: Any?, b: Any): Boolean {
            return a != null && a == b
        }
    }
}