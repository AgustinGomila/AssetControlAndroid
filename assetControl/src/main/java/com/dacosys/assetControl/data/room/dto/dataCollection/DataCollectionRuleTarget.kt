package com.dacosys.assetControl.data.room.dto.dataCollection

import androidx.room.ColumnInfo

class DataCollectionRuleTarget(
    @ColumnInfo(name = Entry.ID) val id: Long = 0,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) val dataCollectionRuleId: Long,
    @ColumnInfo(name = Entry.ASSET_ID) val assetId: Long?,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) val warehouseId: Long?,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long?,
    @ColumnInfo(name = Entry.ITEM_CATEGORY_ID) val itemCategoryId: Long?
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

    object Entry {
        const val TABLE_NAME = "data_collection_rule_target"
        const val ID = "_id"
        const val DATA_COLLECTION_RULE_ID = "data_collection_rule_id"
        const val ASSET_ID = "asset_id"
        const val WAREHOUSE_ID = "warehouse_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val ITEM_CATEGORY_ID = "item_category_id"
    }
}

