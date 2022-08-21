package com.dacosys.assetControl.model.routes.route.wsObject

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class RouteObject() : Parcelable {
    var route_id: Long = 0
    var active = 0
    var description = String()

    constructor(parcel: Parcel) : this() {
        route_id = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString() ?: ""
    }

    fun getBySoapObject(so: SoapObject): RouteObject {
        val x = RouteObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "route_id" -> {
                            x.route_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
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
        parcel.writeLong(route_id)
        parcel.writeInt(active)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteObject> {
        override fun createFromParcel(parcel: Parcel): RouteObject {
            return RouteObject(parcel)
        }

        override fun newArray(size: Int): Array<RouteObject?> {
            return arrayOfNulls(size)
        }
    }
}


