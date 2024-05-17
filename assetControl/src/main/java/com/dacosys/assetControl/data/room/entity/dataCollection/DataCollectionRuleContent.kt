package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.*
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
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) var dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = Entry.LEVEL) var level: Int = 0,
    @ColumnInfo(name = Entry.POSITION) var position: Int = 0,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) var attributeId: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_ID) var attributeCompositionId: Long = 0L,
    @ColumnInfo(name = Entry.EXPRESSION) var expression: String? = null,
    @ColumnInfo(name = Entry.TRUE_RESULT) var trueResult: Int = 0,
    @ColumnInfo(name = Entry.FALSE_RESULT) var falseResult: Int = 0,
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) var mActive: Int = 0,
    @ColumnInfo(name = Entry.MANDATORY) var mMandatory: Int = 0,
    @Ignore var attributeStr: String = ""
) {
    @Ignore
    val active: Boolean = this.mActive == 1

    @Ignore
    val mandatory: Boolean = this.mMandatory == 1

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

        const val ATTRIBUTE_STR = "attribute_str"
    }
}

