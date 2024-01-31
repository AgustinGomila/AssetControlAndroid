package com.dacosys.assetControl.data.webservice.common

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class SessionObject() : Parcelable {
    var sessionId = ""
    var userId = 0L

    constructor(parcel: Parcel) : this() {
        sessionId = parcel.readString() ?: ""
        userId = parcel.readLong()
    }

    fun getBySoapObject(so: SoapObject): SessionObject {
        val x = SessionObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "session_id" -> {
                            x.sessionId = soValue as? String ?: ""
                        }

                        "user_id" -> {
                            x.userId = soValue as? Long ?: 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sessionId)
        parcel.writeLong(userId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SessionObject> {
        override fun createFromParcel(parcel: Parcel): SessionObject {
            return SessionObject(parcel)
        }

        override fun newArray(size: Int): Array<SessionObject?> {
            return arrayOfNulls(size)
        }
    }
}


