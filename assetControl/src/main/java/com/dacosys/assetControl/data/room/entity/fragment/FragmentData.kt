package com.dacosys.assetControl.data.room.entity.fragment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_CONTENT_ID) val dataCollectionRuleContentId: Long,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_TYPE_ID) val attributeCompositionTypeId: Long,
    @ColumnInfo(name = Entry.VALUE_STR) val valueStr: String,
    @ColumnInfo(name = Entry.IS_ENABLED) val isEnabled: Int
) {
    object Entry {
        const val TABLE_NAME = "fragment_data"
        const val DATA_COLLECTION_RULE_CONTENT_ID = "data_collection_rule_content_id"
        const val ATTRIBUTE_COMPOSITION_TYPE_ID = "attribute_composition_type_id"
        const val VALUE_STR = "value_str"
        const val IS_ENABLED = "is_enabled"
    }
}


