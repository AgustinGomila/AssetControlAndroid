package com.dacosys.assetControl.data.room.dto.route

import androidx.room.ColumnInfo

abstract class RouteCompositionEntry {
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

class RouteComposition(
    @ColumnInfo(name = RouteCompositionEntry.ROUTE_ID) val routeId: Long = 0L,
    @ColumnInfo(name = RouteCompositionEntry.DATA_COLLECTION_RULE_ID) val dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = RouteCompositionEntry.LEVEL) val level: Int = 0,
    @ColumnInfo(name = RouteCompositionEntry.POSITION) val position: Int = 0,
    @ColumnInfo(name = RouteCompositionEntry.ASSET_ID) val assetId: Long? = null,
    @ColumnInfo(name = RouteCompositionEntry.WAREHOUSE_ID) val warehouseId: Long? = null,
    @ColumnInfo(name = RouteCompositionEntry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long? = null,
    @ColumnInfo(name = RouteCompositionEntry.EXPRESSION) val expression: String? = null,
    @ColumnInfo(name = RouteCompositionEntry.TRUE_RESULT) val trueResult: Int = 0,
    @ColumnInfo(name = RouteCompositionEntry.FALSE_RESULT) val falseResult: Int = 0
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteComposition

        if (routeId != other.routeId) return false
        if (level != other.level) return false
        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        var result = routeId.hashCode()
        result = 31 * result + level
        result = 31 * result + position
        return result
    }
}
