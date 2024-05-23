package com.dacosys.assetControl.data.room.dto.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Ignore

class DataCollectionRuleContent(
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
    @ColumnInfo(name = Entry.ATTRIBUTE_STR) var attributeStr: String? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_STR) var attributeCompositionStr: String? = null
) {

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataCollectionRuleContent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    @Ignore
    var active: Boolean = mActive == 1
        get() = mActive == 1
        set(value) {
            mActive = if (value) 1 else 0
            field = value
        }

    @Ignore
    var mandatory: Boolean = mMandatory == 1
        get() = mMandatory == 1
        set(value) {
            mMandatory = if (value) 1 else 0
            field = value
        }

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
        const val ATTRIBUTE_COMPOSITION_STR = "attribute_composition_str"
    }
}