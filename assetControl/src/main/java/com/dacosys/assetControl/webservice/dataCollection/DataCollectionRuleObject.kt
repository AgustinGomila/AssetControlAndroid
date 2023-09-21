package com.dacosys.assetControl.webservice.dataCollection

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class DataCollectionRuleObject() : Parcelable {
    var dataCollectionRuleId: Long = 0
    var active = 0
    var description = String()

    constructor(parcel: Parcel) : this() {
        dataCollectionRuleId = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString() ?: ""
    }

    fun getBySoapObject(so: SoapObject): DataCollectionRuleObject {
        val x = DataCollectionRuleObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "data_collection_rule_id" -> {
                            x.dataCollectionRuleId = soValue as? Long ?: 0L
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
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeInt(active)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionRuleObject> {
        override fun createFromParcel(parcel: Parcel): DataCollectionRuleObject {
            return DataCollectionRuleObject(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionRuleObject?> {
            return arrayOfNulls(size)
        }
    }
}


