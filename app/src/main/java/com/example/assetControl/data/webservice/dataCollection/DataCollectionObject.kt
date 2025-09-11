package com.example.assetControl.data.webservice.dataCollection

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.data.room.dto.dataCollection.DataCollection
import com.example.assetControl.utils.misc.DateUtils.formatDateToString
import org.ksoap2.serialization.SoapObject

class DataCollectionObject() : Parcelable {
    var dataCollectionId: Long = 0
    var assetId: Long = 0
    var warehouseId: Long = 0
    var warehouseAreaId: Long = 0
    var userId: Long = 0
    var dateStart: String = ""
    var dateEnd: String = ""
    var completed: Int = 0
    var transferedDate: String = ""
    var collectorDataCollectionId: Long = 0
    var collectorRouteProcessId: Long = 0

    constructor(parcel: Parcel) : this() {
        dataCollectionId = parcel.readLong()
        assetId = parcel.readLong()
        warehouseId = parcel.readLong()
        warehouseAreaId = parcel.readLong()
        userId = parcel.readLong()
        dateStart = parcel.readString().orEmpty()
        dateEnd = parcel.readString().orEmpty()
        completed = parcel.readInt()
        transferedDate = parcel.readString().orEmpty()
        collectorDataCollectionId = parcel.readLong()
        collectorRouteProcessId = parcel.readLong()
    }

    constructor(dc: DataCollection) : this() {
        dataCollectionId = dc.dataCollectionId
        assetId = dc.assetId ?: 0
        warehouseId = dc.warehouseId ?: 0
        warehouseAreaId = dc.warehouseAreaId ?: 0
        userId = dc.userId
        dateStart = formatDateToString(dc.dateStart)
        dateEnd = formatDateToString(dc.dateEnd)
        completed = dc.completed
        transferedDate = formatDateToString(dc.transferredDate)
        collectorDataCollectionId = dc.id
        collectorRouteProcessId = dc.routeProcessId
    }

    fun getBySoapObject(so: SoapObject): DataCollectionObject {
        val x = DataCollectionObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "data_collection_id" -> {
                            x.dataCollectionId = soValue as? Long ?: 0L
                        }

                        "asset_id" -> {
                            x.assetId = soValue as? Long ?: 0L
                        }

                        "warehouse_id" -> {
                            x.warehouseId = soValue as? Long ?: 0L
                        }

                        "warehouse_area_id" -> {
                            x.warehouseAreaId = soValue as? Long ?: 0L
                        }

                        "user_id" -> {
                            x.userId = soValue as? Long ?: 0L
                        }

                        "date_start" -> {
                            x.dateStart = soValue as? String ?: ""
                        }

                        "date_end" -> {
                            x.dateEnd = soValue as? String ?: ""
                        }

                        "completed" -> {
                            x.completed = soValue as? Int ?: 0
                        }

                        "transfered_date" -> {
                            x.transferedDate = soValue as? String ?: ""
                        }

                        "collector_data_collection_id" -> {
                            x.collectorDataCollectionId = soValue as? Long ?: 0L
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
        parcel.writeLong(dataCollectionId)
        parcel.writeLong(assetId)
        parcel.writeLong(warehouseId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeLong(userId)
        parcel.writeString(dateStart)
        parcel.writeString(dateEnd)
        parcel.writeInt(completed)
        parcel.writeString(transferedDate)
        parcel.writeLong(collectorDataCollectionId)
        parcel.writeLong(collectorRouteProcessId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionObject> {
        override fun createFromParcel(parcel: Parcel): DataCollectionObject {
            return DataCollectionObject(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionObject?> {
            return arrayOfNulls(size)
        }
    }
}