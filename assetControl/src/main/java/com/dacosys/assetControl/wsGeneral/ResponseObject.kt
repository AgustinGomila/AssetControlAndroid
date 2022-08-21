package com.dacosys.assetControl.wsGeneral

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class ResponseObject() : Parcelable {
    var resultCode = 0
    var caption = ""
    var message = ""

    constructor(parcel: Parcel) : this() {
        resultCode = parcel.readInt()
        caption = parcel.readString() ?: ""
        message = parcel.readString() ?: ""
    }

    fun getBySoapObject(so: SoapObject): ResponseObject {
        val x = ResponseObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "result_code" -> {
                            x.resultCode = soValue as? Int ?: 0
                        }
                        "caption" -> {
                            x.caption = soValue as? String ?: ""
                        }
                        "message" -> {
                            x.message = soValue as? String ?: ""
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(resultCode)
        parcel.writeString(caption)
        parcel.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResponseObject> {
        override fun createFromParcel(parcel: Parcel): ResponseObject {
            return ResponseObject(parcel)
        }

        override fun newArray(size: Int): Array<ResponseObject?> {
            return arrayOfNulls(size)
        }
    }
}


