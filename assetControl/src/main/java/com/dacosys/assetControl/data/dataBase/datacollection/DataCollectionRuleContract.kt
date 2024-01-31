package com.dacosys.assetControl.data.dataBase.datacollection

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object DataCollectionRuleContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            DataCollectionRuleEntry.DATA_COLLECTION_RULE_ID,
            DataCollectionRuleEntry.DESCRIPTION,
            DataCollectionRuleEntry.ACTIVE
        )
    }

    abstract class DataCollectionRuleEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "data_collection_rule"

            const val DATA_COLLECTION_RULE_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
        }
    }
}
