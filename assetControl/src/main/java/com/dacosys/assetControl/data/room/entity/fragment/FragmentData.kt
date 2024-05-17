package com.dacosys.assetControl.data.room.entity.fragment

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.fragment.FragmentData.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DATA_COLLECTION_RULE_CONTENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_CONTENT_ID}"
        ),
        Index(
            value = [Entry.ATTRIBUTE_COMPOSITION_TYPE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_COMPOSITION_TYPE_ID}"
        )
    ]
)
data class FragmentData(
    @PrimaryKey
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_CONTENT_ID) val dataCollectionRuleContentId: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_TYPE_ID) val attributeCompositionTypeId: Long = 0L,
    @ColumnInfo(name = Entry.VALUE_STR) val valueStr: String = "",
    @ColumnInfo(name = Entry.IS_ENABLED) val mEnabled: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        dataCollectionRuleContentId = parcel.readLong(),
        attributeCompositionTypeId = parcel.readLong(),
        valueStr = parcel.readString().orEmpty(),
        mEnabled = parcel.readInt()
    )

    @Ignore
    val isEnabled: Boolean = this.mEnabled == 1

    object Entry {
        const val TABLE_NAME = "fragment_data"
        const val DATA_COLLECTION_RULE_CONTENT_ID = "data_collection_rule_content_id"
        const val ATTRIBUTE_COMPOSITION_TYPE_ID = "attribute_composition_type_id"
        const val VALUE_STR = "value_str"
        const val IS_ENABLED = "is_enabled"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionRuleContentId)
        parcel.writeLong(attributeCompositionTypeId)
        parcel.writeString(valueStr)
        parcel.writeInt(mEnabled)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FragmentData> {
        override fun createFromParcel(parcel: Parcel): FragmentData {
            return FragmentData(parcel)
        }

        override fun newArray(size: Int): Array<FragmentData?> {
            return arrayOfNulls(size)
        }
    }
}


