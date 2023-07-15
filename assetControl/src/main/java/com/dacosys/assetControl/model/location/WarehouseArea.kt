package com.dacosys.assetControl.model.location

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.ACTIVE
import com.dacosys.assetControl.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.TRANSFERRED
import com.dacosys.assetControl.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.dataBase.location.WarehouseAreaDbHelper
import com.dacosys.assetControl.webservice.location.WarehouseAreaObject

class WarehouseArea : Parcelable {
    var warehouseAreaId: Long = 0
    private var dataRead: Boolean = false

    val warehouse: Warehouse?
        get() = if (!dataRead && !refreshData()) {
            null
        } else when {
            warehouseId != 0L -> Warehouse(warehouseId, false)
            else -> null
        }

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        warehouseAreaId: Long,
        description: String,
        active: Boolean,
        warehouseId: Long,
        transferred: Boolean,
    ) {
        this.warehouseAreaId = warehouseAreaId
        this.description = description
        this.active = active
        this.warehouseId = warehouseId
        this.transferred = transferred

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        warehouseAreaId = id
        if (doChecks) refreshData()
    }

    constructor(wao: WarehouseAreaObject) {
        this.warehouseAreaId = wao.warehouse_area_id
        this.description = wao.description
        this.active = wao.active == 1
        this.warehouseId = wao.warehouse_id
        this.transferred = false

        dataRead = true
    }

    private fun refreshData(): Boolean {
        val temp = WarehouseAreaDbHelper().selectById(this.warehouseAreaId)

        dataRead = true
        return when {
            temp != null -> {
                warehouseAreaId = temp.warehouseAreaId
                active = temp.active
                description = temp.description
                warehouseId = temp.warehouseId
                transferred = temp.transferred
                warehouseStr = temp.warehouseStr

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

    var warehouseStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var warehouseId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var transferred: Boolean? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    constructor()

    constructor(parcel: android.os.Parcel) {
        warehouseAreaId = parcel.readLong()
        warehouseId = parcel.readLong()
        description = parcel.readString() ?: ""
        active = parcel.readByte() != 0.toByte()
        transferred = parcel.readByte() != 0.toByte()
        warehouseStr = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(WAREHOUSE_AREA_ID, warehouseAreaId)
        values.put(DESCRIPTION, description)
        values.put(ACTIVE, active)
        values.put(WAREHOUSE_ID, warehouseId)
        values.put(TRANSFERRED, transferred)

        //values.put(WAREHOUSE_STR, warehouseStr)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return WarehouseAreaDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is WarehouseArea) {
            false
        } else equals(this.warehouseAreaId, other.warehouseAreaId)
    }

    override fun hashCode(): Int {
        return this.warehouseAreaId.hashCode()
    }

    class CustomComparator : Comparator<WarehouseArea> {
        override fun compare(o1: WarehouseArea, o2: WarehouseArea): Int {
            if (o1.warehouseAreaId < o2.warehouseAreaId) {
                return -1
            } else if (o1.warehouseAreaId > o2.warehouseAreaId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(warehouseAreaId)
        parcel.writeLong(warehouseId)
        parcel.writeString(description)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeByte(if (transferred != null && transferred == true) 1 else 0)
        parcel.writeString(warehouseStr)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseArea> {
        override fun createFromParcel(parcel: android.os.Parcel): WarehouseArea {
            return WarehouseArea(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseArea?> {
            return arrayOfNulls(size)
        }
    }
}