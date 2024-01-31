package com.dacosys.assetControl.data.dataBase.route

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object RouteProcessContentContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            RouteProcessContentEntry.ROUTE_PROCESS_ID,
            RouteProcessContentEntry.DATA_COLLECTION_RULE_ID,
            RouteProcessContentEntry.LEVEL,
            RouteProcessContentEntry.POSITION,
            RouteProcessContentEntry.ROUTE_PROCESS_STATUS_ID,
            RouteProcessContentEntry.DATA_COLLECTION_ID,
            RouteProcessContentEntry.ROUTE_PROCESS_CONTENT_ID

        )
    }

    abstract class RouteProcessContentEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "route_process_content"

            /*
            CREATE TABLE [route_process_content] (
            [route_process_id] bigint NULL ,
            [data_collection_rule_id] bigint NULL ,
            [level] int NULL ,
            [position] int NULL ,
            [route_process_status_id] bigint NULL ,
            [data_collection_id] bigint NULL ,
            [route_process_content_id] bigint NULL )
            */

            const val ROUTE_PROCESS_ID = "route_process_id"
            const val DATA_COLLECTION_RULE_ID = "data_collection_rule_id"
            const val LEVEL = "level"
            const val POSITION = "position"
            const val ROUTE_PROCESS_STATUS_ID = "route_process_status_id"
            const val DATA_COLLECTION_ID = "data_collection_id"
            const val ROUTE_PROCESS_CONTENT_ID = "route_process_content_id"

            // Otras columnas que no proviene de la propia tabla
            const val ROUTE_ID = "route_id"
            const val ROUTE_STR = "route_str"
            const val COLLECTOR_ROUTE_PROCESS_ID = "collector_route_process_id"
            const val ASSET_ID = "asset_id"
            const val ASSET_STR = "asset_str"
            const val ASSET_CODE = "asset_code"
            const val WAREHOUSE_AREA_ID = "warehouse_area_id"
            const val WAREHOUSE_AREA_STR = "warehouse_area_str"
            const val WAREHOUSE_ID = "warehouse_id"
            const val WAREHOUSE_STR = "warehouse_str"
            const val ROUTE_PROCESS_STATUS_STR = "route_process_status_str"
        }
    }
}
