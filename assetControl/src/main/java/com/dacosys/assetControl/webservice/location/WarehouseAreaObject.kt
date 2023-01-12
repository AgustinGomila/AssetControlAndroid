package com.dacosys.assetControl.webservice.location

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.model.location.WarehouseArea
import org.ksoap2.serialization.SoapObject

class WarehouseAreaObject() : Parcelable {
    var warehouse_area_id: Long = 0
    var warehouse_id: Long = 0
    var active = 0
    var description = String()
    var warehouse_area_ext_id = String()

    constructor(parcel: Parcel) : this() {
        warehouse_area_id = parcel.readLong()
        warehouse_id = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString() ?: ""
        warehouse_area_ext_id = parcel.readString() ?: ""
    }

    constructor(warehouseArea: WarehouseArea) : this() {
        // Main Information
        description = warehouseArea.description
        warehouse_area_id = warehouseArea.warehouseAreaId
        warehouse_id = warehouseArea.warehouseId
        active = if (warehouseArea.active) {
            1
        } else {
            0
        }
    }

    fun getBySoapObject(so: SoapObject): WarehouseAreaObject {
        val x = WarehouseAreaObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "warehouse_area_id" -> {
                            x.warehouse_area_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }
                        "warehouse_id" -> {
                            x.warehouse_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }
                        "warehouse_area_ext_id" -> {
                            x.warehouse_area_ext_id = soValue as? String ?: ""
                        }
                        "active" -> {
                            x.active = soValue as? Int ?: 0
                        }
                        "description" -> {
                            x.description = soValue as? String ?: ""
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(warehouse_area_id)
        parcel.writeLong(warehouse_id)
        parcel.writeInt(active)
        parcel.writeString(description)
        parcel.writeString(warehouse_area_ext_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseAreaObject> {
        override fun createFromParcel(parcel: Parcel): WarehouseAreaObject {
            return WarehouseAreaObject(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseAreaObject?> {
            return arrayOfNulls(size)
        }
    }
}


