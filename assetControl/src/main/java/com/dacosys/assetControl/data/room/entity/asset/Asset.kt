package com.dacosys.assetControl.data.room.entity.asset

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.asset.Asset.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.CODE], name = "IDX_${Entry.TABLE_NAME}_${Entry.CODE}"),
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"),
        Index(
            value = [Entry.ITEM_CATEGORY_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ITEM_CATEGORY_ID}"
        ),
        Index(value = [Entry.WAREHOUSE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}"),
        Index(
            value = [Entry.WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}"
        ),
        Index(value = [Entry.SERIAL_NUMBER], name = "IDX_${Entry.TABLE_NAME}_${Entry.SERIAL_NUMBER}"),
        Index(value = [Entry.EAN], name = "IDX_${Entry.TABLE_NAME}_${Entry.EAN}")
    ]
)
data class Asset(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.CODE) val code: String,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) val warehouseId: Long,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.OWNERSHIP_STATUS) val ownershipStatus: Int,
    @ColumnInfo(name = Entry.STATUS) val status: Int,
    @ColumnInfo(name = Entry.MISSING_DATE) val missingDate: String?,
    @ColumnInfo(name = Entry.ITEM_CATEGORY_ID) val itemCategoryId: Long,
    @ColumnInfo(name = Entry.TRANSFERRED) val transferred: Int?,
    @ColumnInfo(name = Entry.ORIGINAL_WAREHOUSE_ID) val originalWarehouseId: Long,
    @ColumnInfo(name = Entry.ORIGINAL_WAREHOUSE_AREA_ID) val originalWarehouseAreaId: Long,
    @ColumnInfo(name = Entry.LABEL_NUMBER) val labelNumber: Int?,
    @ColumnInfo(name = Entry.MANUFACTURER) val manufacturer: String?,
    @ColumnInfo(name = Entry.MODEL) val model: String?,
    @ColumnInfo(name = Entry.SERIAL_NUMBER) val serialNumber: String?,
    @ColumnInfo(name = Entry.CONDITION) val condition: Int?,
    @ColumnInfo(name = Entry.COST_CENTRE_ID) val costCentreId: Long?,
    @ColumnInfo(name = Entry.PARENT_ID) val parentId: Long?,
    @ColumnInfo(name = Entry.EAN) val ean: String?,
    @ColumnInfo(name = Entry.LAST_ASSET_REVIEW_DATE) val lastAssetReviewDate: String?
) {
    object Entry {
        const val TABLE_NAME = "asset"
        const val ID = "_id"
        const val CODE = "code"
        const val DESCRIPTION = "description"
        const val WAREHOUSE_ID = "warehouse_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val ACTIVE = "active"
        const val OWNERSHIP_STATUS = "ownership_status"
        const val STATUS = "status"
        const val MISSING_DATE = "missing_date"
        const val ITEM_CATEGORY_ID = "item_category_id"
        const val TRANSFERRED = "transfered"
        const val ORIGINAL_WAREHOUSE_ID = "original_warehouse_id"
        const val ORIGINAL_WAREHOUSE_AREA_ID = "original_warehouse_area_id"
        const val LABEL_NUMBER = "label_number"
        const val MANUFACTURER = "manufacturer"
        const val MODEL = "model"
        const val SERIAL_NUMBER = "serial_number"
        const val CONDITION = "condition"
        const val COST_CENTRE_ID = "cost_centre_id"
        const val PARENT_ID = "parent_id"
        const val EAN = "ean"
        const val LAST_ASSET_REVIEW_DATE = "last_asset_review_date"
    }
}
