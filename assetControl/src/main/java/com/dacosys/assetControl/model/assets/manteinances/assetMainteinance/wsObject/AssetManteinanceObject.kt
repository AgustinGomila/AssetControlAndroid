package com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.wsObject

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.`object`.AssetManteinance
import org.ksoap2.serialization.SoapObject

class AssetManteinanceObject() : Parcelable {
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

    constructor(assetManteinance: AssetManteinance) : this() {
        // Main Information
        asset_manteinance_id = assetManteinance.assetManteinanceId
        manteinance_type_id = assetManteinance.manteinanceTypeId
        manteinance_status_id = assetManteinance.manteinanceStatusId
        asset_id = assetManteinance.assetId
        repairman_id = Statics.currentUserId!!
    }

    fun getBySoapObject(so: SoapObject): AssetManteinanceObject {
        val x = AssetManteinanceObject()

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

    companion object CREATOR : Parcelable.Creator<AssetManteinanceObject> {
        override fun createFromParcel(parcel: Parcel): AssetManteinanceObject {
            return AssetManteinanceObject(parcel)
        }

        override fun newArray(size: Int): Array<AssetManteinanceObject?> {
            return arrayOfNulls(size)
        }
    }
}


