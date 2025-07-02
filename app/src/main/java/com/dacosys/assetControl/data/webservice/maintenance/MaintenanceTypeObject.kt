package com.dacosys.assetControl.data.webservice.maintenance

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.data.room.dto.maintenance.MaintenanceType
import org.ksoap2.serialization.SoapObject

class MaintenanceTypeObject() : Parcelable {
    var manteinance_type_id: Long = 0
    var manteinance_type_group_id: Long = 0
    var active = 0
    var description = String()

    constructor(parcel: Parcel) : this() {
        manteinance_type_id = parcel.readLong()
        manteinance_type_group_id = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString().orEmpty()
    }

    constructor(maintenanceType: MaintenanceType) : this() {
        // Main Information
        description = maintenanceType.description
        manteinance_type_id = maintenanceType.id
        manteinance_type_group_id = maintenanceType.maintenanceTypeGroupId
        active = maintenanceType.active
    }

    fun getBySoapObject(so: SoapObject): MaintenanceTypeObject {
        val x = MaintenanceTypeObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "manteinance_type_id" -> {
                            x.manteinance_type_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "manteinance_type_group_id" -> {
                            x.manteinance_type_group_id =
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
        parcel.writeLong(manteinance_type_id)
        parcel.writeLong(manteinance_type_group_id)
        parcel.writeInt(active)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MaintenanceTypeObject> {
        override fun createFromParcel(parcel: Parcel): MaintenanceTypeObject {
            return MaintenanceTypeObject(parcel)
        }

        override fun newArray(size: Int): Array<MaintenanceTypeObject?> {
            return arrayOfNulls(size)
        }
    }
}


