package com.example.assetControl.data.room.dto.dataCollection

import androidx.room.ColumnInfo

abstract class DataCollectionRuleTargetEntry {
    companion object {
        const val TABLE_NAME = "data_collection_rule_target"
        const val ID = "_id"
        const val DATA_COLLECTION_RULE_ID = "data_collection_rule_id"
        const val ASSET_ID = "asset_id"
        const val WAREHOUSE_ID = "warehouse_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val ITEM_CATEGORY_ID = "item_category_id"
    }
}

class DataCollectionRuleTarget(
    @ColumnInfo(name = DataCollectionRuleTargetEntry.ID) val id: Long = 0,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.DATA_COLLECTION_RULE_ID) val dataCollectionRuleId: Long,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.ASSET_ID) val assetId: Long?,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.WAREHOUSE_ID) val warehouseId: Long?,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long?,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.ITEM_CATEGORY_ID) val itemCategoryId: Long?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataCollectionRuleTarget

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

