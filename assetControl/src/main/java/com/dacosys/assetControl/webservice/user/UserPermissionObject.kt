package com.dacosys.assetControl.webservice.user

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class UserPermissionObject() : Parcelable {
    var permission_id = 0L
    var user_id = 0L

    constructor(parcel: Parcel) : this() {
        permission_id = parcel.readLong()
        user_id = parcel.readLong()
    }

    fun getBySoapObject(so: SoapObject): UserPermissionObject {
        val x = UserPermissionObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "permission_id" -> {
                            x.permission_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }
                        "user_id" -> {
                            x.user_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(permission_id)
        parcel.writeLong(user_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserPermissionObject> {
        override fun createFromParcel(parcel: Parcel): UserPermissionObject {
            return UserPermissionObject(parcel)
        }

        override fun newArray(size: Int): Array<UserPermissionObject?> {
            return arrayOfNulls(size)
        }
    }
}


