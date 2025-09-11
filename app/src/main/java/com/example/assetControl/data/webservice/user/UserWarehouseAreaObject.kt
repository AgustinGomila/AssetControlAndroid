package com.example.assetControl.data.webservice.user

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class UserWarehouseAreaObject() : Parcelable {
    var warehouse_area_id: Long = 0
    var user_id: Long = 0
    var see = 0
    var move = 0
    var check = 0
    var count = 0

    constructor(parcel: Parcel) : this() {
        warehouse_area_id = parcel.readLong()
        user_id = parcel.readLong()
        see = parcel.readInt()
        move = parcel.readInt()
        check = parcel.readInt()
        count = parcel.readInt()
    }

    fun getBySoapObject(so: SoapObject): UserWarehouseAreaObject {
        val x = UserWarehouseAreaObject()

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

                        "user_id" -> {
                            x.user_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "see" -> {
                            x.see = soValue as? Int ?: 0
                        }

                        "move" -> {
                            x.move = soValue as? Int ?: 0
                        }

                        "check" -> {
                            x.check = soValue as? Int ?: 0
                        }

                        "count" -> {
                            x.count = soValue as? Int ?: 0
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(warehouse_area_id)
        parcel.writeLong(user_id)
        parcel.writeInt(see)
        parcel.writeInt(move)
        parcel.writeInt(check)
        parcel.writeInt(count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserWarehouseAreaObject> {
        override fun createFromParcel(parcel: Parcel): UserWarehouseAreaObject {
            return UserWarehouseAreaObject(parcel)
        }

        override fun newArray(size: Int): Array<UserWarehouseAreaObject?> {
            return arrayOfNulls(size)
        }
    }
}


