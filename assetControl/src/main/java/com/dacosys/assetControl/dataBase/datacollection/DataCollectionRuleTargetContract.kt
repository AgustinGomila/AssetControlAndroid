package com.dacosys.assetControl.dataBase.datacollection

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object DataCollectionRuleTargetContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            DataCollectionRuleTargetEntry.ITEM_CATEGORY_ID,
            DataCollectionRuleTargetEntry.DATA_COLLECTION_RULE_ID,
            DataCollectionRuleTargetEntry.WAREHOUSE_ID,
            DataCollectionRuleTargetEntry.ASSET_ID,
            DataCollectionRuleTargetEntry.WAREHOUSE_AREA_ID
        )
    }

    abstract class DataCollectionRuleTargetEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "data_collection_rule_target"

            const val DATA_COLLECTION_RULE_ID = "data_collection_rule_id"
            const val ITEM_CATEGORY_ID = "item_category_id"
            const val ASSET_ID = "asset_id"
            const val WAREHOUSE_ID = "warehouse_id"
            const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        }
    }
}
