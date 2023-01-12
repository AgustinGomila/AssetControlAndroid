package com.dacosys.assetControl.webservice.attribute

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class AttributeObject() : Parcelable {
    var attributeId: Long = 0
    var attributeTypeId: Long = 0
    var active = 0
    var description = String()
    var attributeCategoryId: Long = 0

    constructor(parcel: Parcel) : this() {
        attributeId = parcel.readLong()
        attributeTypeId = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString() ?: ""
        attributeCategoryId = parcel.readLong()
    }

    fun getBySoapObject(so: SoapObject): AttributeObject {
        val x = AttributeObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "attribute_id" -> {
                            x.attributeId = soValue as? Long ?: 0L
                        }
                        "attribute_type_id" -> {
                            x.attributeTypeId = soValue as? Long ?: 0L
                        }
                        "attribute_category_id" -> {
                            x.attributeCategoryId = soValue as? Long ?: 0L
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
        parcel.writeLong(attributeId)
        parcel.writeLong(attributeTypeId)
        parcel.writeInt(active)
        parcel.writeString(description)
        parcel.writeLong(attributeCategoryId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AttributeObject> {
        override fun createFromParcel(parcel: Parcel): AttributeObject {
            return AttributeObject(parcel)
        }

        override fun newArray(size: Int): Array<AttributeObject?> {
            return arrayOfNulls(size)
        }
    }
}


