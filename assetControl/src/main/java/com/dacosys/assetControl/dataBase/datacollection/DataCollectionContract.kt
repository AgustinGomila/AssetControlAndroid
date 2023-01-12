package com.dacosys.assetControl.dataBase.datacollection

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object DataCollectionContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            DataCollectionEntry.DATA_COLLECTION_ID,
            DataCollectionEntry.ASSET_ID,
            DataCollectionEntry.WAREHOUSE_ID,
            DataCollectionEntry.WAREHOUSE_AREA_ID,
            DataCollectionEntry.USER_ID,
            DataCollectionEntry.DATE_START,
            DataCollectionEntry.DATE_END,
            DataCollectionEntry.COMPLETED,
            DataCollectionEntry.TRANSFERED_DATE,
            DataCollectionEntry.COLLECTOR_DATA_COLLECTION_ID,
            DataCollectionEntry.COLLECTOR_ROUTE_PROCESS_ID
        )
    }

    abstract class DataCollectionEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "data_collection"

            const val DATA_COLLECTION_ID = "data_collection_id"
            const val ASSET_ID = "asset_id"
            const val WAREHOUSE_ID = "warehouse_id"
            const val WAREHOUSE_AREA_ID = "warehouse_area_id"
            const val USER_ID = "user_id"
            const val DATE_START = "date_start"
            const val DATE_END = "date_end"
            const val COMPLETED = "completed"
            const val TRANSFERED_DATE = "transfered_date"
            const val COLLECTOR_DATA_COLLECTION_ID = "_id"
            const val COLLECTOR_ROUTE_PROCESS_ID = "collector_route_process_id"

            const val ASSET_CODE = "asset_code"
            const val ASSET_STR = "asset_str"
            const val WAREHOUSE_STR = "warehouse_str"
            const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        }
    }
}
