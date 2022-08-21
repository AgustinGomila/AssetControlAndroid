package com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object DataCollectionContentContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            DataCollectionContentEntry.DATA_COLLECTION_ID,
            DataCollectionContentEntry.LEVEL,
            DataCollectionContentEntry.POSITION,
            DataCollectionContentEntry.ATTRIBUTE_ID,
            DataCollectionContentEntry.ATTRIBUTE_COMPOSITION_ID,
            DataCollectionContentEntry.RESULT,
            DataCollectionContentEntry.VALUE_STR,
            DataCollectionContentEntry.DATA_COLLECTION_DATE,
            DataCollectionContentEntry.DATA_COLLECTION_CONTENT_ID,
            DataCollectionContentEntry.COLLECTOR_DATA_COLLECTION_CONTENT_ID,
            DataCollectionContentEntry.DATA_COLLECTION_RULE_CONTENT_ID

        )
    }

    abstract class DataCollectionContentEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "data_collection_content"

            const val DATA_COLLECTION_ID = "data_collection_id"
            const val LEVEL = "level"
            const val POSITION = "position"
            const val ATTRIBUTE_ID = "attribute_id"
            const val ATTRIBUTE_COMPOSITION_ID = "attribute_composition_id"
            const val RESULT = "result"
            const val VALUE_STR = "value_str"
            const val DATA_COLLECTION_DATE = "data_collection_date"
            const val DATA_COLLECTION_CONTENT_ID = "data_collection_content_id"
            const val COLLECTOR_DATA_COLLECTION_CONTENT_ID = "_id"
            const val DATA_COLLECTION_RULE_CONTENT_ID = "data_collection_rule_content_id"
        }
    }
}
