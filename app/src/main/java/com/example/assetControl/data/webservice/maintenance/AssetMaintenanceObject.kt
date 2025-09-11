package com.example.assetControl.data.webservice.maintenance

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.data.room.dto.maintenance.AssetMaintenance
import org.ksoap2.serialization.SoapObject

class AssetMaintenanceObject() : Parcelable {
    var asset_manteinance_id = 0L
    var manteinance_type_id = 0L
    var manteinance_status_id = 0
    var asset_id = 0L
    var repairman_id = 0L

    constructor(parcel: Parcel) : this() {
        asset_manteinance_id = parcel.readLong()
        manteinance_type_id = parcel.readLong()
        manteinance_status_id = parcel.readInt()
        asset_id = parcel.readLong()
        repairman_id = parcel.readLong()
    }

    constructor(assetMaintenance: AssetMaintenance, userId: Long) : this() {
        asset_manteinance_id = assetMaintenance.id
        manteinance_type_id = assetMaintenance.maintenanceTypeId
        manteinance_status_id = assetMaintenance.maintenanceStatusId
        asset_id = assetMaintenance.assetId
        repairman_id = userId
    }

    fun getBySoapObject(so: SoapObject): AssetMaintenanceObject {
        val x = AssetMaintenanceObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "asset_manteinance_id" -> {
                            x.asset_manteinance_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "manteinance_type_id" -> {
                            x.manteinance_type_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "manteinance_status_id" -> {
                            x.manteinance_status_id =
                                soValue as? Int ?: (soValue as? Long)?.toInt() ?: 0
                        }

                        "asset_id" -> {
                            x.asset_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "repairman_id" -> {
                            x.repairman_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(asset_manteinance_id)
        parcel.writeLong(manteinance_type_id)
        parcel.writeInt(manteinance_status_id)
        parcel.writeLong(asset_id)
        parcel.writeLong(repairman_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetMaintenanceObject> {
        override fun createFromParcel(parcel: Parcel): AssetMaintenanceObject {
            return AssetMaintenanceObject(parcel)
        }

        override fun newArray(size: Int): Array<AssetMaintenanceObject?> {
            return arrayOfNulls(size)
        }
    }
}


