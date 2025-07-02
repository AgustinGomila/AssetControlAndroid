package com.dacosys.assetControl.data.webservice.movement

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovementContent
import org.ksoap2.serialization.SoapObject

class WarehouseMovementContentObject() : Parcelable {
    var warehouseMovementId: Long = 0
    var warehouseMovementContentId: Long = 0
    var assetId: Long = 0
    var code: String = ""
    var qty: Float = 0F

    constructor(parcel: Parcel) : this() {
        warehouseMovementId = parcel.readLong()
        warehouseMovementContentId = parcel.readLong()
        assetId = parcel.readLong()
        code = parcel.readString().orEmpty()
        qty = parcel.readFloat()
    }

    constructor(wmc: WarehouseMovementContent) : this() {
        warehouseMovementId = wmc.warehouseMovementId
        warehouseMovementContentId = wmc.id
        assetId = wmc.assetId
        qty = wmc.qty?.toFloat() ?: 0F
        code = wmc.code
    }

    fun getBySoapObject(so: SoapObject): WarehouseMovementContentObject {
        val x = WarehouseMovementContentObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "warehouse_movement_id" -> {
                            x.warehouseMovementId = soValue as? Long ?: 0L
                        }

                        "warehouse_movement_content_id" -> {
                            x.warehouseMovementContentId = soValue as? Long ?: 0L
                        }

                        "asset_id" -> {
                            x.assetId = soValue as? Long ?: 0L
                        }

                        "code" -> {
                            x.code = soValue as? String ?: ""
                        }

                        "qty" -> {
                            x.qty = soValue as? Float ?: 0F
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(warehouseMovementId)
        parcel.writeLong(warehouseMovementContentId)
        parcel.writeLong(assetId)
        parcel.writeString(code)
        parcel.writeFloat(qty)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseMovementContentObject> {
        override fun createFromParcel(parcel: Parcel): WarehouseMovementContentObject {
            return WarehouseMovementContentObject(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseMovementContentObject?> {
            return arrayOfNulls(size)
        }
    }
}