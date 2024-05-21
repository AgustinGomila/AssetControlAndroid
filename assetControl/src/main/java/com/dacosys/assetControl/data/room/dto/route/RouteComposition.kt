package com.dacosys.assetControl.data.room.dto.route

import androidx.room.ColumnInfo

class RouteComposition(
    @ColumnInfo(name = Entry.ROUTE_ID) val routeId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) val dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = Entry.LEVEL) val level: Int = 0,
    @ColumnInfo(name = Entry.POSITION) val position: Int = 0,
    @ColumnInfo(name = Entry.ASSET_ID) val assetId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) val warehouseId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long? = null,
    @ColumnInfo(name = Entry.EXPRESSION) val expression: String? = null,
    @ColumnInfo(name = Entry.TRUE_RESULT) val trueResult: Int = 0,
    @ColumnInfo(name = Entry.FALSE_RESULT) val falseResult: Int = 0
) {

    object Entry {
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
