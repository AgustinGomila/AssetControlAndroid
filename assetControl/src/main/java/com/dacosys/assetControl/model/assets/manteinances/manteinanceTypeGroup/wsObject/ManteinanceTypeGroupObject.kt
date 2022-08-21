package com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.wsObject

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class ManteinanceTypeGroupObject() : Parcelable {
    var manteinanceTypeGroupId = 0L
    var active = 0
    var description = String()

    constructor(parcel: Parcel) : this() {
        manteinanceTypeGroupId = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString() ?: ""
    }

    fun getBySoapObject(so: SoapObject): ManteinanceTypeGroupObject {
        val x = ManteinanceTypeGroupObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "manteinance_type_group_id" -> {
                            x.manteinanceTypeGroupId = soValue as? Long ?: 0L
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
        parcel.writeLong(manteinanceTypeGroupId)
        parcel.writeInt(active)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ManteinanceTypeGroupObject> {
        override fun createFromParcel(parcel: Parcel): ManteinanceTypeGroupObject {
            return ManteinanceTypeGroupObject(parcel)
        }

        override fun newArray(size: Int): Array<ManteinanceTypeGroupObject?> {
            return arrayOfNulls(size)
        }
    }
}


