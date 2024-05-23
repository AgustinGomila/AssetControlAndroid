package com.dacosys.assetControl.data.room.dto.dataCollection

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.enums.attribute.AttributeCompositionType
import com.dacosys.assetControl.utils.misc.UTCDataTime.Companion.dateToStringDate
import com.dacosys.assetControl.utils.misc.UTCDataTime.Companion.getUTCDateTimeAsNotNullDate
import com.dacosys.assetControl.utils.misc.UTCDataTime.Companion.stringDateToNotNullDate
import java.util.*

class DataCollectionContent(
    @ColumnInfo(name = Entry.DATA_COLLECTION_CONTENT_ID) var dataCollectionContentId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) var dataCollectionId: Long? = null,
    @ColumnInfo(name = Entry.LEVEL) var level: Int? = null,
    @ColumnInfo(name = Entry.POSITION) var position: Int? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) var attributeId: Long? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_ID) var attributeCompositionId: Long? = null,
    @ColumnInfo(name = Entry.RESULT) var result: Int? = null,
    @ColumnInfo(name = Entry.VALUE_STR) var valueString: String? = null,
    @ColumnInfo(name = Entry.DATA_COLLECTION_DATE) var dataCollectionDate: Date = Date(),
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_CONTENT_ID) var dataCollectionRuleContentId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_CONTENT_STR) var dataCollectionRuleContentStr: String? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_STR) var attributeCompositionStr: String? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_STR) var attributeStr: String? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_TYPE_ID) var attributeCompositionTypeId: Int? = null
) : Parcelable {

    @Ignore
    var valueStr = valueString.orEmpty()
        get() = valueString.orEmpty()
        set(value) {
            valueString = value.ifEmpty { null }
            field = value
        }

    constructor(parcel: Parcel) : this(
        dataCollectionContentId = parcel.readLong(),
        dataCollectionId = parcel.readValue(Long::class.java.classLoader) as? Long,
        level = parcel.readValue(Int::class.java.classLoader) as? Int,
        position = parcel.readValue(Int::class.java.classLoader) as? Int,
        attributeId = parcel.readValue(Long::class.java.classLoader) as? Long,
        attributeCompositionId = parcel.readValue(Long::class.java.classLoader) as? Long,
        result = parcel.readValue(Int::class.java.classLoader) as? Int,
        valueString = parcel.readString().orEmpty(),
        dataCollectionDate = stringDateToNotNullDate(parcel.readString().orEmpty()),
        id = parcel.readLong(),
        dataCollectionRuleContentId = parcel.readLong(),
        dataCollectionRuleContentStr = parcel.readString().orEmpty(),
        attributeCompositionStr = parcel.readString().orEmpty(),
        attributeStr = parcel.readString().orEmpty(),
        attributeCompositionTypeId = parcel.readValue(Int::class.java.classLoader) as? Int,
    )

    constructor(
        ruleContent: DataCollectionRuleContent,
        attributeCompositionType: AttributeCompositionType?,
        virtualId: Long,
        anyResult: Any?,
        valueStr: String,
    ) : this() {
        if (ruleContent.attributeCompositionId > 0) {
            attributeCompositionId = ruleContent.attributeCompositionId
            attributeCompositionStr = ruleContent.attributeCompositionStr
        }

        if (ruleContent.attributeId > 0) {
            attributeId = ruleContent.attributeId
            attributeStr = ruleContent.attributeStr
        }

        val attrTypeId = attributeCompositionType?.id
        if (attrTypeId != null) {
            attributeCompositionTypeId = attrTypeId.toInt()
        }

        dataCollectionContentId = virtualId
        dataCollectionDate = getUTCDateTimeAsNotNullDate()
        dataCollectionRuleContentId = ruleContent.id
        level = ruleContent.level
        position = ruleContent.position

        if (anyResult != null) {
            if (anyResult is Int) {
                result = anyResult.toInt()
            } else if (anyResult is Boolean) {
                result = if (anyResult) 1 else 0
            }
        }
        valueString = valueStr
    }

    object Entry {
        const val TABLE_NAME = "data_collection_content"
        const val DATA_COLLECTION_CONTENT_ID = "data_collection_content_id"
        const val DATA_COLLECTION_ID = "data_collection_id"
        const val LEVEL = "level"
        const val POSITION = "position"
        const val ATTRIBUTE_ID = "attribute_id"
        const val ATTRIBUTE_COMPOSITION_ID = "attribute_composition_id"
        const val RESULT = "result"
        const val VALUE_STR = "value_str"
        const val DATA_COLLECTION_DATE = "data_collection_date"
        const val ID = "_id"
        const val DATA_COLLECTION_RULE_CONTENT_ID = "data_collection_rule_content_id"

        const val DATA_COLLECTION_RULE_CONTENT_STR = "data_collection_rule_content_str"
        const val ATTRIBUTE_STR = "attribute_str"
        const val ATTRIBUTE_COMPOSITION_STR = "attribute_composition_str"
        const val ATTRIBUTE_COMPOSITION_TYPE_ID = "attribute_composition_type_id"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionContentId)
        parcel.writeValue(dataCollectionId)
        parcel.writeValue(level)
        parcel.writeValue(position)
        parcel.writeValue(attributeId)
        parcel.writeValue(attributeCompositionId)
        parcel.writeValue(result)
        parcel.writeString(valueString)
        parcel.writeString(dateToStringDate(dataCollectionDate))
        parcel.writeLong(id)
        parcel.writeLong(dataCollectionRuleContentId)
        parcel.writeString(dataCollectionRuleContentStr)
        parcel.writeString(attributeCompositionStr)
        parcel.writeString(attributeStr)
        parcel.writeValue(attributeCompositionTypeId)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataCollectionContent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionContent> {
        override fun createFromParcel(parcel: Parcel): DataCollectionContent {
            return DataCollectionContent(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionContent?> {
            return arrayOfNulls(size)
        }
    }
}