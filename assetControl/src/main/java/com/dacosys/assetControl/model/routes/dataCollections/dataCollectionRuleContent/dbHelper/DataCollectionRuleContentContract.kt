package com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleContent.dbHelper

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object DataCollectionRuleContentContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            DataCollectionRuleContentEntry.DATA_COLLECTION_RULE_CONTENT_ID,
            DataCollectionRuleContentEntry.DESCRIPTION,
            DataCollectionRuleContentEntry.DATA_COLLECTION_RULE_ID,
            DataCollectionRuleContentEntry.LEVEL,
            DataCollectionRuleContentEntry.POSITION,
            DataCollectionRuleContentEntry.ATTRIBUTE_ID,
            DataCollectionRuleContentEntry.ATTRIBUTE_COMPOSITION_ID,
            DataCollectionRuleContentEntry.EXPRESSION,
            DataCollectionRuleContentEntry.TRUE_RESULT,
            DataCollectionRuleContentEntry.FALSE_RESULT,
            DataCollectionRuleContentEntry.ACTIVE,
            DataCollectionRuleContentEntry.MANDATORY
        )
    }

    abstract class DataCollectionRuleContentEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "data_collection_rule_content"

            const val DATA_COLLECTION_RULE_CONTENT_ID = "_id"
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
}
