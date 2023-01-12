package com.dacosys.assetControl.webservice.route

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class RouteProcessContentObject() : Parcelable {
    var routeProcessId: Long = 0
    var dataCollectionRuleId: Long = 0
    var level: Int = 0
    var position: Int = 0
    var routeProcessStatusId: Int = 0
    var dataCollectionId: Long = 0
    var routeProcessContentId: Long = 0

    constructor(parcel: Parcel) : this() {
        this.routeProcessId = parcel.readLong()
        this.dataCollectionRuleId = parcel.readLong()
        this.level = parcel.readInt()
        this.position = parcel.readInt()
        this.routeProcessStatusId = parcel.readInt()
        this.dataCollectionId = parcel.readLong()
        this.routeProcessContentId = parcel.readLong()
    }

    fun getBySoapObject(so: SoapObject): RouteProcessContentObject {
        val x = RouteProcessContentObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "route_process_id" -> {
                            x.routeProcessId = soValue as? Long ?: 0L
                        }
                        "data_collection_rule_id" -> {
                            x.dataCollectionRuleId = soValue as? Long ?: 0L
                        }
                        "level" -> {
                            x.level = soValue as? Int ?: 0
                        }
                        "position" -> {
                            x.position = soValue as? Int ?: 0
                        }
                        "route_process_status_id" -> {
                            x.routeProcessStatusId = soValue as? Int ?: 0
                        }
                        "data_collection_id" -> {
                            x.dataCollectionId = soValue as? Long ?: 0L
                        }
                        "route_process_content_id" -> {
                            x.routeProcessContentId = soValue as? Long ?: 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(routeProcessId)
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeInt(routeProcessStatusId)
        parcel.writeLong(dataCollectionId)
        parcel.writeLong(routeProcessContentId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteProcessContentObject> {
        override fun createFromParcel(parcel: Parcel): RouteProcessContentObject {
            return RouteProcessContentObject(parcel)
        }

        override fun newArray(size: Int): Array<RouteProcessContentObject?> {
            return arrayOfNulls(size)
        }
    }
}