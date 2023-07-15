package com.dacosys.assetControl.webservice.user

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.model.user.User
import org.ksoap2.serialization.SoapObject

class UserObject() : Parcelable {
    var user_id = 0L
    var active = 0
    var name = String()
    var external_id = String()
    var email = String()
    var password = String()

    constructor(parcel: Parcel) : this() {
        user_id = parcel.readLong()
        active = parcel.readInt()
        name = parcel.readString() ?: ""
        external_id = parcel.readString() ?: ""
        email = parcel.readString() ?: ""
        password = parcel.readString() ?: ""
    }

    fun getBySoapObject(so: SoapObject): UserObject {
        val x = UserObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "user_id" -> {
                            x.user_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "password" -> {
                            x.password = soValue as? String ?: ""
                        }

                        "active" -> {
                            x.active = soValue as? Int ?: 0
                        }

                        "name" -> {
                            x.name = soValue as? String ?: ""
                        }

                        "external_id" -> {
                            x.external_id = soValue as? String ?: ""
                        }

                        "email" -> {
                            x.email = soValue as? String ?: ""
                        }
                    }
            }
        }
        return x
    }

    fun getByUser(user: User): UserObject {
        val x = UserObject()

        x.user_id = user.userId
        x.active = if (user.active) 1 else 0
        x.name = user.name
        x.external_id = user.externalId ?: ""
        x.email = user.email
        x.password = user.password

        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(user_id)
        parcel.writeInt(active)
        parcel.writeString(name)
        parcel.writeString(external_id)
        parcel.writeString(email)
        parcel.writeString(password)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserObject> {
        override fun createFromParcel(parcel: Parcel): UserObject {
            return UserObject(parcel)
        }

        override fun newArray(size: Int): Array<UserObject?> {
            return arrayOfNulls(size)
        }
    }
}