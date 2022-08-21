package com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper

import android.provider.BaseColumns

class FragmentDataContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            FragmentDataEntry.DATA_COLLECTION_RULE_CONTENT_ID,
            FragmentDataEntry.ATTRIBUTE_COMPOSITION_TYPE_ID,
            FragmentDataEntry.VALUE_STR,
            FragmentDataEntry.IS_ENABLED

        )
    }

    abstract class FragmentDataEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "fragment_data"

            const val VALUE_STR = "value_str"
            const val DATA_COLLECTION_RULE_CONTENT_ID = "data_collection_rule_content_id"
            const val ATTRIBUTE_COMPOSITION_TYPE_ID = "attribute_composition_type_id"
            const val IS_ENABLED = "is_enabled"
        }
    }
}