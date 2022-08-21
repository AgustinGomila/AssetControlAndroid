package com.dacosys.assetControl.model.routes.routeComposition.dbHelper

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object RouteCompositionContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            RouteCompositionEntry.ROUTE_ID,
            RouteCompositionEntry.DATA_COLLECTION_RULE_ID,
            RouteCompositionEntry.LEVEL,
            RouteCompositionEntry.POSITION,
            RouteCompositionEntry.ASSET_ID,
            RouteCompositionEntry.WAREHOUSE_ID,
            RouteCompositionEntry.WAREHOUSE_AREA_ID,
            RouteCompositionEntry.EXPRESSION,
            RouteCompositionEntry.TRUE_RESULT,
            RouteCompositionEntry.FALSE_RESULT
        )
    }

    abstract class RouteCompositionEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "route_composition"

            const val ROUTE_ID = "route_id"
            const val DATA_COLLECTION_RULE_ID = "data_collection_rule_id"
            const val LEVEL = "level"
            const val POSITION = "position"
            const val ASSET_ID = "asset_id"
            const val WAREHOUSE_ID = "warehouse_id"
            const val WAREHOUSE_AREA_ID = "warehouse_area_id"
            const val EXPRESSION = "expression"
            const val TRUE_RESULT = "true_result"
            const val FALSE_RESULT = "false_result"
        }
    }
}
