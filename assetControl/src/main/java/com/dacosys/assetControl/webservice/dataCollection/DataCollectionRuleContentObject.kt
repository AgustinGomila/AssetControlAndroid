package com.dacosys.assetControl.webservice.dataCollection

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class DataCollectionRuleContentObject() : Parcelable {
    var dataCollectionRuleContentId: Long = 0
    var description: String = ""
    var dataCollectionRuleId: Long = 0
    var level: Int = 0
    var position: Int = 0
    var attributeId: Long = 0
    var attributeCompositionId: Long = 0
    var expression: String = ""
    var trueResult: Int = 0
    var falseResult: Int = 0
    var active: Int = 0
    var mandatory: Int = 0

    constructor(parcel: Parcel) : this() {
        this.dataCollectionRuleContentId = parcel.readLong()
        this.description = parcel.readString() ?: ""
        this.dataCollectionRuleId = parcel.readLong()
        this.level = parcel.readInt()
        this.position = parcel.readInt()
        this.attributeId = parcel.readLong()
        this.attributeCompositionId = parcel.readLong()
        this.expression = parcel.readString() ?: ""
        this.trueResult = parcel.readInt()
        this.falseResult = parcel.readInt()
        this.active = parcel.readInt()
        this.mandatory = parcel.readInt()
    }

    fun getBySoapObject(so: SoapObject): DataCollectionRuleContentObject {
        val x = DataCollectionRuleContentObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "data_collection_rule_content_id" -> {
                            x.dataCollectionRuleContentId = soValue as? Long ?: 0L
                        }
                        "data_collection_rule_id" -> {
                            x.dataCollectionRuleId = soValue as? Long ?: 0L
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
                        "expression" -> {
                            x.expression = soValue as? String ?: ""
                        }
                        "true_result" -> {
                            x.trueResult = soValue as? Int ?: 0
                        }
                        "false_result" -> {
                            x.falseResult = soValue as? Int ?: 0
                        }
                        "description" -> {
                            x.description = soValue as? String ?: ""
                        }
                        "active" -> {
                            x.active = soValue as? Int ?: 0
                        }
                        "mandatory" -> {
                            x.mandatory = soValue as? Int ?: 0
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionRuleContentId)
        parcel.writeString(description)
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeLong(attributeId)
        parcel.writeLong(attributeCompositionId)
        parcel.writeString(expression)
        parcel.writeInt(trueResult)
        parcel.writeInt(falseResult)
        parcel.writeInt(active)
        parcel.writeInt(mandatory)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionRuleContentObject> {
        override fun createFromParcel(parcel: Parcel): DataCollectionRuleContentObject {
            return DataCollectionRuleContentObject(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionRuleContentObject?> {
            return arrayOfNulls(size)
        }
    }
}