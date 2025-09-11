package com.example.assetControl.data.room.entity.fragment

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

abstract class FragmentDataEntry {
    companion object {
        const val TABLE_NAME = "fragment_data"
        const val DATA_COLLECTION_RULE_CONTENT_ID = "data_collection_rule_content_id"
        const val ATTRIBUTE_COMPOSITION_TYPE_ID = "attribute_composition_type_id"
        const val VALUE_STR = "value_str"
        const val IS_ENABLED = "is_enabled"
    }
}

@Entity(
    tableName = FragmentDataEntry.TABLE_NAME,
    indices = [
        Index(
            value = [FragmentDataEntry.DATA_COLLECTION_RULE_CONTENT_ID],
            name = "IDX_${FragmentDataEntry.TABLE_NAME}_${FragmentDataEntry.DATA_COLLECTION_RULE_CONTENT_ID}"
        ),
        Index(
            value = [FragmentDataEntry.ATTRIBUTE_COMPOSITION_TYPE_ID],
            name = "IDX_${FragmentDataEntry.TABLE_NAME}_${FragmentDataEntry.ATTRIBUTE_COMPOSITION_TYPE_ID}"
        )
    ]
)
data class FragmentDataEntity(
    @PrimaryKey
    @ColumnInfo(name = FragmentDataEntry.DATA_COLLECTION_RULE_CONTENT_ID) val dataCollectionRuleContentId: Long = 0L,
    @ColumnInfo(name = FragmentDataEntry.ATTRIBUTE_COMPOSITION_TYPE_ID) val attributeCompositionTypeId: Long = 0L,
    @ColumnInfo(name = FragmentDataEntry.VALUE_STR) val valueStr: String = "",
    @ColumnInfo(name = FragmentDataEntry.IS_ENABLED) val mEnabled: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        dataCollectionRuleContentId = parcel.readLong(),
        attributeCompositionTypeId = parcel.readLong(),
        valueStr = parcel.readString().orEmpty(),
        mEnabled = parcel.readInt()
    )

    @Ignore
    val isEnabled: Boolean = mEnabled == 1

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionRuleContentId)
        parcel.writeLong(attributeCompositionTypeId)
        parcel.writeString(valueStr)
        parcel.writeInt(mEnabled)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FragmentDataEntity> {
        override fun createFromParcel(parcel: Parcel): FragmentDataEntity {
            return FragmentDataEntity(parcel)
        }

        override fun newArray(size: Int): Array<FragmentDataEntity?> {
            return arrayOfNulls(size)
        }
    }
}


