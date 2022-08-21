package com.dacosys.assetControl.model.assets.attributes.attributeCategory.wsObject

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class AttributeCategoryObject() : Parcelable {
    var attributeCategoryId = 0L
    var parentId = 0L
    var active = 0
    var description = String()

    constructor(parcel: Parcel) : this() {
        attributeCategoryId = parcel.readLong()
        parentId = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString() ?: ""
    }

    fun getBySoapObject(so: SoapObject): AttributeCategoryObject {
        val x = AttributeCategoryObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "attribute_category_id" -> {
                            x.attributeCategoryId = soValue as? Long ?: 0L
                        }
                        "parent_id" -> {
                            x.parentId = soValue as? Long ?: 0L
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
        parcel.writeLong(attributeCategoryId)
        parcel.writeLong(parentId)
        parcel.writeInt(active)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AttributeCategoryObject> {
        override fun createFromParcel(parcel: Parcel): AttributeCategoryObject {
            return AttributeCategoryObject(parcel)
        }

        override fun newArray(size: Int): Array<AttributeCategoryObject?> {
            return arrayOfNulls(size)
        }
    }
}


