package com.dacosys.assetControl.webservice.attribute

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class AttributeCompositionObject() : Parcelable {
    var attributeCompositionId: Long = 0
    var attributeId: Long = 0
    var attributeCompositionTypeId: Long = 0
    var description: String = ""
    var composition: String = ""
    var used: Int = 0
    var name: String = ""
    var readOnly: Int = 0
    var defaultValue: String = ""

    constructor(parcel: Parcel) : this() {
        attributeCompositionId = parcel.readLong()
        attributeId = parcel.readLong()
        attributeCompositionTypeId = parcel.readLong()
        description = parcel.readString() ?: ""
        composition = parcel.readString() ?: ""
        used = parcel.readInt()
        name = parcel.readString() ?: ""
        readOnly = parcel.readInt()
        defaultValue = parcel.readString() ?: ""
    }

    fun getBySoapObject(so: SoapObject): AttributeCompositionObject {
        val x = AttributeCompositionObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "attribute_composition_id" -> {
                            x.attributeCompositionId = soValue as? Long ?: 0L
                        }
                        "attribute_id" -> {
                            x.attributeId = soValue as? Long ?: 0L
                        }
                        "attribute_composition_type_id" -> {
                            x.attributeCompositionTypeId = soValue as? Long ?: 0L
                        }
                        "used" -> {
                            x.used = soValue as? Int ?: 0
                        }
                        "read_only" -> {
                            x.readOnly = soValue as? Int ?: 0
                        }
                        "description" -> {
                            x.description = soValue as? String ?: ""
                        }
                        "composition" -> {
                            x.composition = soValue as? String ?: ""
                        }
                        "name" -> {
                            x.name = soValue as? String ?: ""
                        }
                        "default_value" -> {
                            x.defaultValue = soValue as? String ?: ""
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(attributeCompositionId)
        parcel.writeLong(attributeId)
        parcel.writeLong(attributeCompositionTypeId)
        parcel.writeString(description)
        parcel.writeString(composition)
        parcel.writeInt(used)
        parcel.writeString(name)
        parcel.writeInt(readOnly)
        parcel.writeString(defaultValue)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AttributeCompositionObject> {
        override fun createFromParcel(parcel: Parcel): AttributeCompositionObject {
            return AttributeCompositionObject(parcel)
        }

        override fun newArray(size: Int): Array<AttributeCompositionObject?> {
            return arrayOfNulls(size)
        }
    }
}