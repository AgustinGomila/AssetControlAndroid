package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContent.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DATA_COLLECTION_RULE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_ID}"
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
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class DataCollectionRuleContent(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) val dataCollectionRuleId: Long,
    @ColumnInfo(name = Entry.LEVEL) val level: Int,
    @ColumnInfo(name = Entry.POSITION) val position: Int,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) val attributeId: Long?,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_ID) val attributeCompositionId: Long?,
    @ColumnInfo(name = Entry.EXPRESSION) val expression: String?,
    @ColumnInfo(name = Entry.TRUE_RESULT) val trueResult: Int?,
    @ColumnInfo(name = Entry.FALSE_RESULT) val falseResult: Int?,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.MANDATORY) val mandatory: Int
) {
    object Entry {
        const val TABLE_NAME = "data_collection_rule_content"
        const val ID = "_id"
        const val DATA_COLLECTION_RULE_ID = "data_collection_rule_id"
        const val LEVEL = "level"
        const val POSITION = "position"
        const val ATTRIBUTE_ID = "attribute_id"
        const val ATTRIBUTE_COMPOSITION_ID = "attribute_composition_id"
        const val EXPRESSION = "expression"
        const val TRUE_RESULT = "true_result"
        const val FALSE_RESULT = "false_result"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val MANDATORY = "mandatory"
    }
}

