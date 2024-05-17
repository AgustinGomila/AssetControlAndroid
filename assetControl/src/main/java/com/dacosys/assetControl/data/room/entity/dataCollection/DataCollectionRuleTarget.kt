package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleTarget.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DATA_COLLECTION_RULE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_ID}"
        ),
        Index(
            value = [Entry.ASSET_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [Entry.ITEM_CATEGORY_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ITEM_CATEGORY_ID}"
        )
    ]
)
data class DataCollectionRuleTarget(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Entry.ID) val id: Long = 0,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) val dataCollectionRuleId: Long,
    @ColumnInfo(name = Entry.ASSET_ID) val assetId: Long?,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) val warehouseId: Long?,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long?,
    @ColumnInfo(name = Entry.ITEM_CATEGORY_ID) val itemCategoryId: Long?
) {
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

