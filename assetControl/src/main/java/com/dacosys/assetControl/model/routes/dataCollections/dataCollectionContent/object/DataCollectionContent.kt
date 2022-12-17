package com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.`object`

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.ATTRIBUTE_COMPOSITION_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.ATTRIBUTE_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.COLLECTOR_DATA_COLLECTION_CONTENT_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.DATA_COLLECTION_CONTENT_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.DATA_COLLECTION_DATE
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.DATA_COLLECTION_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.DATA_COLLECTION_RULE_CONTENT_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.LEVEL
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.POSITION
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.RESULT
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentContract.DataCollectionContentEntry.Companion.VALUE_STR
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleContent.`object`.DataCollectionRuleContent
import com.dacosys.assetControl.utils.misc.UTCDataTime

class DataCollectionContent : Parcelable {
    var collectorDataCollectionContentId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        dataCollectionId: Long,
        level: Int,
        position: Int,
        attributeId: Long,
        attributeCompositionId: Long,
        result: Int,
        valueStr: String,
        dataCollectionDate: String,
        dataCollectionContentId: Long,
        collectorDataCollectionContentId: Long,
        dataCollectionRuleContentId: Long,
    ) {
        this.dataCollectionId = dataCollectionId
        this.level = level
        this.position = position
        this.attributeId = attributeId
        this.attributeCompositionId = attributeCompositionId
        this.result = result
        this.valueStr = valueStr
        this.dataCollectionDate = dataCollectionDate
        this.dataCollectionContentId = dataCollectionContentId
        this.collectorDataCollectionContentId = collectorDataCollectionContentId
        this.dataCollectionRuleContentId = dataCollectionRuleContentId

        dataRead = true
    }

    constructor(
        dcrc: DataCollectionRuleContent,
        virtualId: Long,
        result: Any?,
        valueStr: String,
    ) {
        if (dcrc.attributeCompositionId > 0) {
            this.attributeCompositionId = dcrc.attributeCompositionId
        }

        if (dcrc.attributeId > 0) {
            this.attributeId = dcrc.attributeId
        }

        this.collectorDataCollectionContentId = virtualId
        this.dataCollectionDate = UTCDataTime.getUTCDateTimeAsString()
        this.dataCollectionRuleContentId = dcrc.dataCollectionRuleContentId
        this.level = dcrc.level
        this.position = dcrc.position

        if (result != null) {
            if (result is Int) {
                this.result = result.toInt()
            } else if (result is Boolean) {
                this.result = if (result) {
                    1
                } else {
                    0
                }
            }
        }

        this.valueStr = valueStr
        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        collectorDataCollectionContentId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = DataCollectionContentDbHelper().selectById(this.collectorDataCollectionContentId)

        dataRead = true
        return when {
            temp != null -> {
                this.dataCollectionId = temp.dataCollectionId
                this.level = temp.level
                this.position = temp.position
                this.attributeId = temp.attributeId
                this.attributeCompositionId = temp.attributeCompositionId
                this.result = temp.result
                this.valueStr = temp.valueStr
                this.dataCollectionDate = temp.dataCollectionDate
                this.dataCollectionContentId = temp.dataCollectionContentId
                this.collectorDataCollectionContentId = temp.collectorDataCollectionContentId
                this.dataCollectionRuleContentId = temp.dataCollectionRuleContentId

                true
            }
            else -> false
        }
    }

    /*
    val dataCollection: DataCollection?
        get() {
            return if (dataCollectionId == null || dataCollectionId == 0L) {
                null
            } else DataCollection( dataCollectionId!!, false)
        }
    */

    var dataCollectionContentId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var dataCollectionRuleContentId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var dataCollectionId: Long? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    /*
    val attribute: Attribute?
        get() {
            return if (attributeId == 0L) {
                null
            } else Attribute( attributeId, false)
        }
    */

    var attributeId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    /*
    val attributeComposition: AttributeComposition?
        get() {
            return if (attributeCompositionId == 0L) {
                null
            } else AttributeComposition( attributeCompositionId, false)
        }
    */

    var attributeCompositionId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var level: Int = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var position: Int = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var result: Int = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var valueStr: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    var dataCollectionDate: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    constructor(parcel: android.os.Parcel) {
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

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(DATA_COLLECTION_ID, dataCollectionId)
        values.put(LEVEL, level)
        values.put(POSITION, position)
        values.put(ATTRIBUTE_ID, attributeId)
        values.put(ATTRIBUTE_COMPOSITION_ID, attributeCompositionId)
        values.put(RESULT, result)
        values.put(VALUE_STR, valueStr)
        values.put(DATA_COLLECTION_DATE, dataCollectionDate)
        values.put(DATA_COLLECTION_CONTENT_ID, dataCollectionContentId)
        values.put(COLLECTOR_DATA_COLLECTION_CONTENT_ID, collectorDataCollectionContentId)
        values.put(DATA_COLLECTION_RULE_CONTENT_ID, dataCollectionRuleContentId)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return DataCollectionContentDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is DataCollectionContent) {
            false
        } else equals(this.collectorDataCollectionContentId, other.collectorDataCollectionContentId)
    }

    override fun hashCode(): Int {
        return this.collectorDataCollectionContentId.hashCode()
    }

    class CustomComparator : Comparator<DataCollectionContent> {
        override fun compare(o1: DataCollectionContent, o2: DataCollectionContent): Int {
            if (o1.collectorDataCollectionContentId < o2.collectorDataCollectionContentId) {
                return -1
            } else if (o1.collectorDataCollectionContentId > o2.collectorDataCollectionContentId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(if (dataCollectionId == null) 0L else dataCollectionId ?: return)
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

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionContent> {
        override fun createFromParcel(parcel: android.os.Parcel): DataCollectionContent {
            return DataCollectionContent(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionContent?> {
            return arrayOfNulls(size)
        }

        fun add(
            dataCollectionId: Long,
            level: Int,
            position: Int,
            attributeId: Long,
            attributeCompositionId: Long,
            result: Int,
            valueStr: String,
            dataCollectionDate: String,
            dataCollectionContentId: Long,
            dataCollectionRuleContentId: Long,
        ): DataCollectionContent? {
            val i = DataCollectionContentDbHelper()
            val newId = i.insert(
                dataCollectionId,
                level,
                position,
                attributeId,
                attributeCompositionId,
                result,
                valueStr,
                dataCollectionDate,
                dataCollectionContentId,
                dataCollectionRuleContentId
            )
            return if (newId < 1) null else i.selectById(newId)
        }
    }
}