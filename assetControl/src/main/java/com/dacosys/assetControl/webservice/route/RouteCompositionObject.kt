package com.dacosys.assetControl.webservice.route

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class RouteCompositionObject() : Parcelable {
    var routeId: Long = 0
    var dataCollectionRuleId: Long = 0
    var level: Int = 0
    var position: Int = 0
    var assetId: Long = 0
    var warehouseId: Long = 0
    var warehouseAreaId: Long = 0
    var expression: String = ""
    var trueResult: Int = 0
    var falseResult: Int = 0

    constructor(parcel: Parcel) : this() {
        routeId = parcel.readLong()
        dataCollectionRuleId = parcel.readLong()
        level = parcel.readInt()
        position = parcel.readInt()
        assetId = parcel.readLong()
        warehouseId = parcel.readLong()
        warehouseAreaId = parcel.readLong()
        expression = parcel.readString() ?: ""
        trueResult = parcel.readInt()
        falseResult = parcel.readInt()
    }

    fun getBySoapObject(so: SoapObject): RouteCompositionObject {
        val x = RouteCompositionObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "route_id" -> {
                            x.routeId = soValue as? Long ?: 0L
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
                        "asset_id" -> {
                            x.assetId = soValue as? Long ?: 0L
                        }
                        "warehouse_id" -> {
                            x.warehouseId = soValue as? Long ?: 0L
                        }
                        "warehouse_area_id" -> {
                            x.warehouseAreaId = soValue as? Long ?: 0L
                        }
                        "expression" -> {
                            x.expression = soValue as? String ?: ""
                        }
                        "true_result" -> {
                            x.trueResult = soValue as? Int ?: 0
                        }
                        "false_result" -> {
                            x.falseResult = soValue as? Int ?: 0
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(routeId)
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeLong(assetId)
        parcel.writeLong(warehouseId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeString(expression)
        parcel.writeInt(trueResult)
        parcel.writeInt(falseResult)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteCompositionObject> {
        override fun createFromParcel(parcel: Parcel): RouteCompositionObject {
            return RouteCompositionObject(parcel)
        }

        override fun newArray(size: Int): Array<RouteCompositionObject?> {
            return arrayOfNulls(size)
        }
    }
}