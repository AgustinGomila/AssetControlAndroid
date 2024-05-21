package com.dacosys.assetControl.data.room.dto.dataCollection

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.dacosys.assetControl.utils.Statics.Companion.toDate
import java.util.*

class DataCollectionContent(
    @ColumnInfo(name = Entry.DATA_COLLECTION_CONTENT_ID) var dataCollectionContentId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) var dataCollectionId: Long? = null,
    @ColumnInfo(name = Entry.LEVEL) var level: Int? = null,
    @ColumnInfo(name = Entry.POSITION) var position: Int? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) var attributeId: Long? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_ID) var attributeCompositionId: Long? = null,
    @ColumnInfo(name = Entry.RESULT) var result: Int? = null,
    @ColumnInfo(name = Entry.VALUE_STR) var valueStr: String = "",
    @ColumnInfo(name = Entry.DATA_COLLECTION_DATE) var dataCollectionDate: Date = Date(),
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_CONTENT_ID) var dataCollectionRuleContentId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_CONTENT_STR) var dataCollectionRuleContentStr: String = "",
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_STR) var attributeCompositionStr: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        dataCollectionContentId = parcel.readLong(),
        dataCollectionId = parcel.readValue(Long::class.java.classLoader) as? Long,
        level = parcel.readValue(Int::class.java.classLoader) as? Int,
        position = parcel.readValue(Int::class.java.classLoader) as? Int,
        attributeId = parcel.readValue(Long::class.java.classLoader) as? Long,
        attributeCompositionId = parcel.readValue(Long::class.java.classLoader) as? Long,
        result = parcel.readValue(Int::class.java.classLoader) as? Int,
        valueStr = parcel.readString().orEmpty(),
        dataCollectionDate = parcel.readString().orEmpty().toDate(),
        id = parcel.readLong(),
        dataCollectionRuleContentId = parcel.readLong(),
        dataCollectionRuleContentStr = parcel.readString().orEmpty(),
        attributeCompositionStr = parcel.readString().orEmpty()
    )

    constructor(
        ruleContent: DataCollectionRuleContent,
        virtualId: Long,
        anyResult: Any?,
        valueString: String,
    ) : this() {
        if (ruleContent.attributeCompositionId > 0) {
            attributeCompositionId = ruleContent.attributeCompositionId
        }

        if (ruleContent.attributeId > 0) {
            attributeId = ruleContent.attributeId
        }

        dataCollectionContentId = virtualId
        dataCollectionDate = Date()
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
        valueStr = valueString
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
        const val ATTRIBUTE_COMPOSITION_STR = "attribute_composition_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionContentId)
        parcel.writeValue(dataCollectionId)
        parcel.writeValue(level)
        parcel.writeValue(position)
        parcel.writeValue(attributeId)
        parcel.writeValue(attributeCompositionId)
        parcel.writeValue(result)
        parcel.writeString(valueStr)
        parcel.writeString(dataCollectionDate.toString())
        parcel.writeLong(id)
        parcel.writeLong(dataCollectionRuleContentId)
        parcel.writeString(dataCollectionRuleContentStr)
        parcel.writeString(attributeCompositionStr)
    }

    override fun describeContents(): Int {
        return 0
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

