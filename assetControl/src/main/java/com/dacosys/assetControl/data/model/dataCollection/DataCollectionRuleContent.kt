package com.dacosys.assetControl.data.model.dataCollection

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.ACTIVE
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.ATTRIBUTE_COMPOSITION_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.ATTRIBUTE_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.DATA_COLLECTION_RULE_CONTENT_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.EXPRESSION
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.FALSE_RESULT
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.LEVEL
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.MANDATORY
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.POSITION
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.TRUE_RESULT
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentDbHelper

class DataCollectionRuleContent : Parcelable {
    var dataCollectionRuleContentId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        dataCollectionRuleContentId: Long,
        description: String,
        dataCollectionRuleId: Long,
        level: Int,
        position: Int,
        attributeId: Long,
        attributeCompositionId: Long,
        expression: String?,
        trueResult: Int,
        falseResult: Int,
        active: Boolean,
        mandatory: Boolean,
    ) {
        this.dataCollectionRuleContentId = dataCollectionRuleContentId
        this.description = description
        this.dataCollectionRuleId = dataCollectionRuleId
        this.level = level
        this.position = position
        this.attributeId = attributeId
        this.attributeCompositionId = attributeCompositionId
        this.expression = expression
        this.trueResult = trueResult
        this.falseResult = falseResult
        this.active = active
        this.mandatory = mandatory

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        dataCollectionRuleContentId = id
        if (doChecks) refreshData()
    }

    fun isAttribute(): Boolean {
        return attributeCompositionId <= 0
    }

    private fun refreshData(): Boolean {
        val temp = DataCollectionRuleContentDbHelper().selectById(this.dataCollectionRuleContentId)

        dataRead = true
        return when {
            temp != null -> {
                this.dataCollectionRuleContentId = temp.dataCollectionRuleContentId
                this.description = temp.description
                this.dataCollectionRuleId = temp.dataCollectionRuleId
                this.level = temp.level
                this.position = temp.position
                this.attributeId = temp.attributeId
                this.attributeCompositionId = temp.attributeCompositionId
                this.expression = temp.expression
                this.trueResult = temp.trueResult
                this.falseResult = temp.falseResult
                this.active = temp.active
                this.mandatory = temp.mandatory

                this.attributeStr = temp.attributeStr

                true
            }

            else -> false
        }
    }

    override fun toString(): String {
        return description
    }

    /*
    val dataCollectionRule: DataCollectionRule?
        get() {
            return if (dataCollectionRuleId == 0L) null
else DataCollectionRule( dataCollectionRuleId, false)
        }

    val attribute: Attribute?
        get() {
            return if (attributeId == 0L) null
else Attribute( attributeId, false)
        }

    val attributeComposition: AttributeComposition?
        get() {
            return if (attributeCompositionId == 0L) null
else AttributeComposition( attributeCompositionId, false)
        }
    */

    var dataCollectionRuleId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var attributeId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var attributeStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var attributeCompositionId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var trueResult: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var falseResult: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var level: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var position: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var description: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var expression: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    var mandatory: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    var active: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    constructor(parcel: android.os.Parcel) {
        this.dataCollectionRuleContentId = parcel.readLong()
        this.description = parcel.readString() ?: ""
        this.dataCollectionRuleId = parcel.readLong()
        this.level = parcel.readInt()
        this.position = parcel.readInt()
        this.attributeId = parcel.readLong()
        this.attributeCompositionId = parcel.readLong()
        this.expression = parcel.readString()
        this.trueResult = parcel.readInt()
        this.falseResult = parcel.readInt()
        this.active = parcel.readByte() != 0.toByte()
        this.mandatory = parcel.readByte() != 0.toByte()

        this.attributeStr = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(DATA_COLLECTION_RULE_CONTENT_ID, dataCollectionRuleContentId)
        values.put(DESCRIPTION, description)
        values.put(DATA_COLLECTION_RULE_ID, dataCollectionRuleId)
        values.put(LEVEL, level)
        values.put(POSITION, position)
        values.put(ATTRIBUTE_ID, attributeId)
        values.put(ATTRIBUTE_COMPOSITION_ID, attributeCompositionId)
        values.put(EXPRESSION, expression)
        values.put(TRUE_RESULT, trueResult)
        values.put(FALSE_RESULT, falseResult)
        values.put(ACTIVE, active)
        values.put(MANDATORY, mandatory)

        //values.put(ATTRIBUTE_STR, attributeStr)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return DataCollectionRuleContentDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is DataCollectionRuleContent) {
            false
        } else equals(this.dataCollectionRuleContentId, other.dataCollectionRuleContentId)
    }

    override fun hashCode(): Int {
        return this.dataCollectionRuleContentId.hashCode()
    }

    class CustomComparator : Comparator<DataCollectionRuleContent> {
        override fun compare(o1: DataCollectionRuleContent, o2: DataCollectionRuleContent): Int {
            if (o1.dataCollectionRuleContentId < o2.dataCollectionRuleContentId) {
                return -1
            } else if (o1.dataCollectionRuleContentId > o2.dataCollectionRuleContentId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
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
        parcel.writeInt(
            when {
                active -> 1
                else -> 0
            }
        )
        parcel.writeInt(
            when {
                mandatory -> 1
                else -> 0
            }
        )

        parcel.writeString(attributeStr)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionRuleContent> {
        override fun createFromParcel(parcel: android.os.Parcel): DataCollectionRuleContent {
            return DataCollectionRuleContent(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionRuleContent?> {
            return arrayOfNulls(size)
        }
    }
}