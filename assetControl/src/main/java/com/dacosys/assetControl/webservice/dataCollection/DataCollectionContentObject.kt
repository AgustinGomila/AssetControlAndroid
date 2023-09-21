package com.dacosys.assetControl.webservice.dataCollection

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class DataCollectionContentObject() : Parcelable {
    var dataCollectionId: Long = 0
    var level: Int = 0
    var position: Int = 0
    var attributeId: Long = 0
    var attributeCompositionId: Long = 0
    var result: Int = 0
    var valueStr: String = ""
    var dataCollectionDate: String = ""
    var dataCollectionContentId: Long = 0
    var collectorDataCollectionContentId: Long = 0
    var dataCollectionRuleContentId: Long = 0

    constructor(parcel: Parcel) : this() {
        this.dataCollectionId = parcel.readLong()
        this.level = parcel.readInt()
        this.position = parcel.readInt()
        this.attributeId = parcel.readLong()
        this.attributeCompositionId = parcel.readLong()
        this.result = parcel.readInt()
        this.valueStr = parcel.readString() ?: ""
        this.dataCollectionDate = parcel.readString() ?: ""
        this.dataCollectionContentId = parcel.readLong()
        this.collectorDataCollectionContentId = parcel.readLong()
        this.dataCollectionRuleContentId = parcel.readLong()
    }

    fun getBySoapObject(so: SoapObject): DataCollectionContentObject {
        val x = DataCollectionContentObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "data_collection_id" -> {
                            x.dataCollectionId = soValue as? Long ?: 0L
                        }

                        "level" -> {
                            x.level = soValue as? Int ?: 0
                        }

                        "position" -> {
                            x.position = soValue as? Int ?: 0
                        }

                        "attribute_id" -> {
                            x.attributeId = soValue as? Long ?: 0L
                        }

                        "attribute_composition_id" -> {
                            x.attributeCompositionId = soValue as? Long ?: 0L
                        }

                        "result" -> {
                            x.result = soValue as? Int ?: 0
                        }

                        "value_str" -> {
                            x.valueStr = soValue as? String ?: ""
                        }

                        "data_collection_date" -> {
                            x.dataCollectionDate = soValue as? String ?: ""
                        }

                        "data_collection_content_id" -> {
                            x.dataCollectionContentId = soValue as? Long ?: 0L
                        }

                        "collector_data_collection_content_id" -> {
                            x.collectorDataCollectionContentId = soValue as? Long ?: 0L
                        }

                        "data_collection_rule_content_id" -> {
                            x.dataCollectionRuleContentId = soValue as? Long ?: 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeLong(attributeId)
        parcel.writeLong(attributeCompositionId)
        parcel.writeInt(result)
        parcel.writeString(valueStr)
        parcel.writeString(dataCollectionDate)
        parcel.writeLong(dataCollectionContentId)
        parcel.writeLong(collectorDataCollectionContentId)
        parcel.writeLong(dataCollectionRuleContentId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionContentObject> {
        override fun createFromParcel(parcel: Parcel): DataCollectionContentObject {
            return DataCollectionContentObject(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionContentObject?> {
            return arrayOfNulls(size)
        }
    }
}