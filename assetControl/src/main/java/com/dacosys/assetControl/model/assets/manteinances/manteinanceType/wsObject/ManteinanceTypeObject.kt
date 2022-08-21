package com.dacosys.assetControl.model.assets.manteinances.manteinanceType.wsObject

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.`object`.ManteinanceType
import org.ksoap2.serialization.SoapObject

class ManteinanceTypeObject() : Parcelable {
    var manteinance_type_id: Long = 0
    var manteinance_type_group_id: Long = 0
    var active = 0
    var description = String()

    constructor(parcel: Parcel) : this() {
        manteinance_type_id = parcel.readLong()
        manteinance_type_group_id = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString() ?: ""
    }

    constructor(manteinanceType: ManteinanceType) : this() {
        // Main Information
        description = manteinanceType.description
        manteinance_type_id = manteinanceType.manteinanceTypeId
        manteinance_type_group_id = manteinanceType.manteinanceTypeGroupId
        active = if (manteinanceType.active) {
            1
        } else {
            0
        }
    }

    fun getBySoapObject(so: SoapObject): ManteinanceTypeObject {
        val x = ManteinanceTypeObject()

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

    companion object CREATOR : Parcelable.Creator<ManteinanceTypeObject> {
        override fun createFromParcel(parcel: Parcel): ManteinanceTypeObject {
            return ManteinanceTypeObject(parcel)
        }

        override fun newArray(size: Int): Array<ManteinanceTypeObject?> {
            return arrayOfNulls(size)
        }
    }
}


