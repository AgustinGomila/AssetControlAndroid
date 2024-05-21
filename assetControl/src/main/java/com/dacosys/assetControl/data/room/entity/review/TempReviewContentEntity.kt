package com.dacosys.assetControl.data.room.entity.review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.review.AssetReviewContent
import com.dacosys.assetControl.data.room.entity.review.TempReviewContentEntity.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ASSET_REVIEW_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_REVIEW_ID}"
        ),
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        ),
        Index(value = [Entry.ASSET_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_ID}"),
        Index(value = [Entry.CODE], name = "IDX_${Entry.TABLE_NAME}_${Entry.CODE}"),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        ),
        Index(
            value = [Entry.ITEM_CATEGORY_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ITEM_CATEGORY_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [Entry.SERIAL_NUMBER],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.SERIAL_NUMBER}"
        ),
        Index(value = [Entry.EAN], name = "IDX_${Entry.TABLE_NAME}_${Entry.EAN}")
    ]
)
data class TempReviewContentEntity(
    @ColumnInfo(name = Entry.ASSET_REVIEW_ID) var assetReviewId: Long,
    @PrimaryKey @ColumnInfo(name = Entry.ID) var id: Long,
    @ColumnInfo(name = Entry.CONTENT_STATUS) val contentStatus: Int,
    @ColumnInfo(name = Entry.ASSET_ID) val assetId: Long,
    @ColumnInfo(name = Entry.CODE) val code: String,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.STATUS, defaultValue = "1") val status: Int,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long,
    @ColumnInfo(name = Entry.LABEL_NUMBER) val labelNumber: Int?,
    @ColumnInfo(name = Entry.PARENT_ID) val parentId: Long?,
    @ColumnInfo(name = Entry.QTY) val qty: Double?,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_STR) val warehouseAreaStr: String,
    @ColumnInfo(name = Entry.WAREHOUSE_STR) val warehouseStr: String,
    @ColumnInfo(name = Entry.ITEM_CATEGORY_ID, defaultValue = "0") val itemCategoryId: Long,
    @ColumnInfo(name = Entry.ITEM_CATEGORY_STR) val itemCategoryStr: String,
    @ColumnInfo(name = Entry.OWNERSHIP_STATUS, defaultValue = "1") val ownershipStatus: Int,
    @ColumnInfo(name = Entry.MANUFACTURER) val manufacturer: String?,
    @ColumnInfo(name = Entry.MODEL) val model: String?,
    @ColumnInfo(name = Entry.SERIAL_NUMBER) val serialNumber: String?,
    @ColumnInfo(name = Entry.EAN) val ean: String?
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

    object Entry {
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