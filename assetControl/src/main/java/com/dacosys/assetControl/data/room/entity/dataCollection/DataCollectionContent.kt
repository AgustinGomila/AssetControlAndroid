package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionContent.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DATA_COLLECTION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_ID}"
        ),
        Index(
            value = [Entry.LEVEL],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.LEVEL}"
        ),
        Index(
            value = [Entry.POSITION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.POSITION}"
        ),
        Index(
            value = [Entry.ATTRIBUTE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_ID}"
        ),
        Index(
            value = [Entry.ATTRIBUTE_COMPOSITION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_COMPOSITION_ID}"
        ),
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        ),
        Index(
            value = [Entry.DATA_COLLECTION_RULE_CONTENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_CONTENT_ID}"
        )
    ]
)
data class DataCollectionContent(
    @PrimaryKey
    @ColumnInfo(name = Entry.DATA_COLLECTION_CONTENT_ID) val dataCollectionContentId: Long,
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) val dataCollectionId: Long,
    @ColumnInfo(name = Entry.LEVEL) val level: Int,
    @ColumnInfo(name = Entry.POSITION) val position: Int,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) val attributeId: Long,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_ID) val attributeCompositionId: Long,
    @ColumnInfo(name = Entry.RESULT) val result: Int,
    @ColumnInfo(name = Entry.VALUE_STR) val valueStr: String,
    @ColumnInfo(name = Entry.DATA_COLLECTION_DATE) val dataCollectionDate: Long,
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_CONTENT_ID) val dataCollectionRuleContentId: Long
) {
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
    }
}

