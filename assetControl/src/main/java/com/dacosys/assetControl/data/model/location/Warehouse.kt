package com.dacosys.assetControl.data.model.location

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.location.WarehouseContract.WarehouseEntry.Companion.ACTIVE
import com.dacosys.assetControl.data.dataBase.location.WarehouseContract.WarehouseEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.data.dataBase.location.WarehouseContract.WarehouseEntry.Companion.TRANSFERRED
import com.dacosys.assetControl.data.dataBase.location.WarehouseContract.WarehouseEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.data.dataBase.location.WarehouseDbHelper
import com.dacosys.assetControl.data.webservice.location.WarehouseObject

class Warehouse : Parcelable {
    var warehouseId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor()

    constructor(
        warehouseId: Long,
        description: String,
        active: Boolean,
        transferred: Boolean,
    ) {
        this.warehouseId = warehouseId
        this.description = description
        this.active = active
        this.transferred = transferred

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        warehouseId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp = WarehouseDbHelper().selectById(this.warehouseId)

        dataRead = true
        return when {
            temp != null -> {
                warehouseId = temp.warehouseId
                active = temp.active
                description = temp.description
                transferred = temp.transferred

                true
            }

            else -> false
        }
    }

    override fun toString(): String {
        return description
    }

    var description: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var active: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    var transferred: Boolean? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    constructor(parcel: Parcel) {
        warehouseId = parcel.readLong()
        active = parcel.readByte() != 0.toByte()
        description = parcel.readString() ?: ""
        transferred = parcel.readByte() != 0.toByte()

        dataRead = parcel.readByte() != 0.toByte()
    }

    constructor(wao: WarehouseObject) {
        this.warehouseId = wao.warehouse_id
        this.active = wao.active == 1
        this.description = wao.description
        this.transferred = false

        dataRead = true
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(WAREHOUSE_ID, warehouseId)
        values.put(DESCRIPTION, description)
        values.put(ACTIVE, active)
        values.put(TRANSFERRED, transferred)
        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return WarehouseDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Warehouse) {
            false
        } else equals(this.warehouseId, other.warehouseId)
    }

    override fun hashCode(): Int {
        return warehouseId.hashCode()
    }

    class CustomComparator : Comparator<Warehouse> {
        override fun compare(o1: Warehouse, o2: Warehouse): Int {
            if (o1.warehouseId < o2.warehouseId) {
                return -1
            } else if (o1.warehouseId > o2.warehouseId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(warehouseId)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(description)
        parcel.writeByte(if (transferred == true) 1 else 0)

        parcel.writeByte(if (dataRead) 1 else 0)
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