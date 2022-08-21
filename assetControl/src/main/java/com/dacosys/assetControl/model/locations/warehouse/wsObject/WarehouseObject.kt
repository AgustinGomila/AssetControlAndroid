package com.dacosys.assetControl.model.locations.warehouse.wsObject

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.model.locations.warehouse.`object`.Warehouse
import org.ksoap2.serialization.SoapObject

class WarehouseObject() : Parcelable {
    var warehouse_id: Long = 0
    var active = 0
    var description = String()
    var warehouse_ext_id = String()

    constructor(parcel: Parcel) : this() {
        warehouse_id = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString() ?: ""
        warehouse_ext_id = parcel.readString() ?: ""
    }

    constructor(warehouse: Warehouse) : this() {
        // Main Information
        description = warehouse.description
        warehouse_id = warehouse.warehouseId
        active = if (warehouse.active) {
            1
        } else {
            0
        }
        warehouse_ext_id = ""
    }

    fun getBySoapObject(so: SoapObject): WarehouseObject {
        val x = WarehouseObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "warehouse_id" -> {
                            x.warehouse_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }
                        "warehouse_ext_id" -> {
                            x.warehouse_ext_id = soValue as? String ?: ""
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
        parcel.writeLong(warehouse_id)
        parcel.writeInt(active)
        parcel.writeString(description)
        parcel.writeString(warehouse_ext_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseObject> {
        override fun createFromParcel(parcel: Parcel): WarehouseObject {
            return WarehouseObject(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseObject?> {
            return arrayOfNulls(size)
        }
    }
}


