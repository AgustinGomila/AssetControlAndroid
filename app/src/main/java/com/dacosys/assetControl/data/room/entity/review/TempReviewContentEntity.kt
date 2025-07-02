package com.dacosys.assetControl.data.room.entity.review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.review.AssetReviewContent

abstract class TempReviewContentEntry {
    companion object {
        const val TABLE_NAME = "temp_asset_review_content"
        const val ASSET_REVIEW_ID = "asset_review_id"
        const val ID = "_id"
        const val CONTENT_STATUS = "content_status"
        const val ASSET_ID = "asset_id"
        const val CODE = "code"
        const val DESCRIPTION = "description"
        const val STATUS = "status"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val LABEL_NUMBER = "label_number"
        const val PARENT_ID = "parent_id"
        const val QTY = "qty"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        const val WAREHOUSE_STR = "warehouse_str"
        const val ITEM_CATEGORY_ID = "item_category_id"
        const val ITEM_CATEGORY_STR = "item_category_str"
        const val OWNERSHIP_STATUS = "ownership_status"
        const val MANUFACTURER = "manufacturer"
        const val MODEL = "model"
        const val SERIAL_NUMBER = "serial_number"
        const val EAN = "ean"
    }
}

@Entity(
    tableName = TempReviewContentEntry.TABLE_NAME,
    indices = [
        Index(
            value = [TempReviewContentEntry.ASSET_REVIEW_ID],
            name = "IDX_${TempReviewContentEntry.TABLE_NAME}_${TempReviewContentEntry.ASSET_REVIEW_ID}"
        ),
        Index(
            value = [TempReviewContentEntry.ID],
            name = "IDX_${TempReviewContentEntry.TABLE_NAME}_${TempReviewContentEntry.ID}"
        ),
        Index(
            value = [TempReviewContentEntry.ASSET_ID],
            name = "IDX_${TempReviewContentEntry.TABLE_NAME}_${TempReviewContentEntry.ASSET_ID}"
        ),
        Index(
            value = [TempReviewContentEntry.CODE],
            name = "IDX_${TempReviewContentEntry.TABLE_NAME}_${TempReviewContentEntry.CODE}"
        ),
        Index(
            value = [TempReviewContentEntry.DESCRIPTION],
            name = "IDX_${TempReviewContentEntry.TABLE_NAME}_${TempReviewContentEntry.DESCRIPTION}"
        ),
        Index(
            value = [TempReviewContentEntry.ITEM_CATEGORY_ID],
            name = "IDX_${TempReviewContentEntry.TABLE_NAME}_${TempReviewContentEntry.ITEM_CATEGORY_ID}"
        ),
        Index(
            value = [TempReviewContentEntry.WAREHOUSE_AREA_ID],
            name = "IDX_${TempReviewContentEntry.TABLE_NAME}_${TempReviewContentEntry.WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [TempReviewContentEntry.SERIAL_NUMBER],
            name = "IDX_${TempReviewContentEntry.TABLE_NAME}_${TempReviewContentEntry.SERIAL_NUMBER}"
        ),
        Index(
            value = [TempReviewContentEntry.EAN],
            name = "IDX_${TempReviewContentEntry.TABLE_NAME}_${TempReviewContentEntry.EAN}"
        )
    ]
)
data class TempReviewContentEntity(
    @ColumnInfo(name = TempReviewContentEntry.ASSET_REVIEW_ID) var assetReviewId: Long,
    @PrimaryKey @ColumnInfo(name = TempReviewContentEntry.ID) var id: Long,
    @ColumnInfo(name = TempReviewContentEntry.CONTENT_STATUS) val contentStatus: Int,
    @ColumnInfo(name = TempReviewContentEntry.ASSET_ID) val assetId: Long,
    @ColumnInfo(name = TempReviewContentEntry.CODE) val code: String,
    @ColumnInfo(name = TempReviewContentEntry.DESCRIPTION) val description: String,
    @ColumnInfo(name = TempReviewContentEntry.STATUS, defaultValue = "1") val status: Int,
    @ColumnInfo(name = TempReviewContentEntry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long,
    @ColumnInfo(name = TempReviewContentEntry.LABEL_NUMBER) val labelNumber: Int?,
    @ColumnInfo(name = TempReviewContentEntry.PARENT_ID) val parentId: Long?,
    @ColumnInfo(name = TempReviewContentEntry.QTY) val qty: Double?,
    @ColumnInfo(name = TempReviewContentEntry.WAREHOUSE_AREA_STR) val warehouseAreaStr: String,
    @ColumnInfo(name = TempReviewContentEntry.WAREHOUSE_STR) val warehouseStr: String,
    @ColumnInfo(name = TempReviewContentEntry.ITEM_CATEGORY_ID, defaultValue = "0") val itemCategoryId: Long,
    @ColumnInfo(name = TempReviewContentEntry.ITEM_CATEGORY_STR) val itemCategoryStr: String,
    @ColumnInfo(name = TempReviewContentEntry.OWNERSHIP_STATUS, defaultValue = "1") val ownershipStatus: Int,
    @ColumnInfo(name = TempReviewContentEntry.MANUFACTURER) val manufacturer: String?,
    @ColumnInfo(name = TempReviewContentEntry.MODEL) val model: String?,
    @ColumnInfo(name = TempReviewContentEntry.SERIAL_NUMBER) val serialNumber: String?,
    @ColumnInfo(name = TempReviewContentEntry.EAN) val ean: String?
) {
    constructor(content: AssetReviewContent) : this(
        assetReviewId = content.assetReviewId,
        id = content.id,
        contentStatus = content.contentStatusId,
        assetId = content.assetId,
        code = content.code,
        description = content.description,
        status = content.assetStatusId,
        warehouseAreaId = content.warehouseAreaId,
        labelNumber = content.labelNumber,
        parentId = content.parentId,
        qty = content.qty,
        warehouseAreaStr = content.warehouseAreaStr,
        warehouseStr = content.warehouseStr,
        itemCategoryId = content.itemCategoryId,
        itemCategoryStr = content.itemCategoryStr,
        ownershipStatus = content.ownershipStatusId,
        manufacturer = content.manufacturer,
        model = content.model,
        serialNumber = content.serialNumber,
        ean = content.ean
    )
}