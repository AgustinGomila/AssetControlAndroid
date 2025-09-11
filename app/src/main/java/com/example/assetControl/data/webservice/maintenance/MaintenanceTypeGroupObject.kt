package com.example.assetControl.data.webservice.maintenance

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class MaintenanceTypeGroupObject() : Parcelable {
    var maintenanceTypeGroupId = 0L
    var active = 0
    var description = String()

    constructor(parcel: Parcel) : this() {
        maintenanceTypeGroupId = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString().orEmpty()
    }

    fun getBySoapObject(so: SoapObject): MaintenanceTypeGroupObject {
        val x = MaintenanceTypeGroupObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "manteinance_type_group_id" -> {
                            x.maintenanceTypeGroupId = soValue as? Long ?: 0L
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
        parcel.writeLong(maintenanceTypeGroupId)
        parcel.writeInt(active)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MaintenanceTypeGroupObject> {
        override fun createFromParcel(parcel: Parcel): MaintenanceTypeGroupObject {
            return MaintenanceTypeGroupObject(parcel)
        }

        override fun newArray(size: Int): Array<MaintenanceTypeGroupObject?> {
            return arrayOfNulls(size)
        }
    }
}


