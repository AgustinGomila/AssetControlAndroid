package com.example.assetControl.data.webservice.route

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.data.room.dto.route.RouteProcess
import com.example.assetControl.utils.misc.DateUtils.formatDateToString
import org.ksoap2.serialization.SoapObject

class RouteProcessObject() : Parcelable {
    var userId: Long = 0
    var routeId: Long = 0
    var routeProcessDate: String = ""
    var completed: Int = 0
    var transfered: Int = 0
    var transferedDate: String = ""
    var routeProcessId: Long = 0
    var collectorRouteProcessId: Long = 0

    constructor(parcel: Parcel) : this() {
        userId = parcel.readLong()
        routeId = parcel.readLong()
        routeProcessDate = parcel.readString().orEmpty()
        completed = parcel.readInt()
        transfered = parcel.readInt()
        transferedDate = parcel.readString().orEmpty()
        routeProcessId = parcel.readLong()
        collectorRouteProcessId = parcel.readLong()
    }

    constructor(rp: RouteProcess) : this() {
        userId = rp.userId
        routeId = rp.routeId
        routeProcessDate = formatDateToString(rp.routeProcessDate)
        completed = if (rp.completed) 1 else 0
        transfered = rp.transferred ?: 0
        transferedDate = formatDateToString(rp.transferredDate)
        routeProcessId = rp.routeProcessId
        collectorRouteProcessId = rp.id
    }

    fun getBySoapObject(so: SoapObject): RouteProcessObject {
        val x = RouteProcessObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "user_id" -> {
                            x.userId = soValue as? Long ?: 0L
                        }

                        "route_id" -> {
                            x.routeId = soValue as? Long ?: 0L
                        }

                        "route_process_date" -> {
                            x.routeProcessDate = soValue as? String ?: ""
                        }

                        "completed" -> {
                            x.completed = soValue as? Int ?: 0
                        }

                        "transfered" -> { // Está mal escrito en el WS
                            x.transfered = soValue as? Int ?: 0
                        }

                        "transfered_date" -> { // Está mal escrito en el WS
                            x.transferedDate = soValue as? String ?: ""
                        }

                        "route_process_id" -> {
                            x.routeProcessId = soValue as? Long ?: 0L
                        }

                        "collector_route_process_id" -> {
                            x.collectorRouteProcessId = soValue as? Long ?: 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(userId)
        parcel.writeLong(routeId)
        parcel.writeString(routeProcessDate)
        parcel.writeInt(completed)
        parcel.writeInt(transfered)
        parcel.writeString(transferedDate)
        parcel.writeLong(routeProcessId)
        parcel.writeLong(collectorRouteProcessId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteProcessObject> {
        override fun createFromParcel(parcel: Parcel): RouteProcessObject {
            return RouteProcessObject(parcel)
        }

        override fun newArray(size: Int): Array<RouteProcessObject?> {
            return arrayOfNulls(size)
        }
    }
}