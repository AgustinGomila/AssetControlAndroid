package com.dacosys.assetControl.data.room.entity.movement

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovementContent

abstract class TempMovementContentEntry {
    companion object {
        const val TABLE_NAME = "temp_warehouse_movement_content"
        const val ID = "_id"
        const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
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
    tableName = TempMovementContentEntry.TABLE_NAME,
    indices = [
        Index(
            value = [TempMovementContentEntry.WAREHOUSE_MOVEMENT_ID],
            name = "IDX_${TempMovementContentEntry.TABLE_NAME}_${TempMovementContentEntry.WAREHOUSE_MOVEMENT_ID}"
        ),
        Index(
            value = [TempMovementContentEntry.ID],
            name = "IDX_${TempMovementContentEntry.TABLE_NAME}_${TempMovementContentEntry.ID}"
        ),
        Index(
            value = [TempMovementContentEntry.ASSET_ID],
            name = "IDX_${TempMovementContentEntry.TABLE_NAME}_${TempMovementContentEntry.ASSET_ID}"
        ),
        Index(
            value = [TempMovementContentEntry.CODE],
            name = "IDX_${TempMovementContentEntry.TABLE_NAME}_${TempMovementContentEntry.CODE}"
        ),
        Index(
            value = [TempMovementContentEntry.DESCRIPTION],
            name = "IDX_${TempMovementContentEntry.TABLE_NAME}_${TempMovementContentEntry.DESCRIPTION}"
        ),
        Index(
            value = [TempMovementContentEntry.ITEM_CATEGORY_ID],
            name = "IDX_${TempMovementContentEntry.TABLE_NAME}_${TempMovementContentEntry.ITEM_CATEGORY_ID}"
        ),
        Index(
            value = [TempMovementContentEntry.WAREHOUSE_AREA_ID],
            name = "IDX_${TempMovementContentEntry.TABLE_NAME}_${TempMovementContentEntry.WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [TempMovementContentEntry.SERIAL_NUMBER],
            name = "IDX_${TempMovementContentEntry.TABLE_NAME}_${TempMovementContentEntry.SERIAL_NUMBER}"
        ),
        Index(
            value = [TempMovementContentEntry.EAN],
            name = "IDX_${TempMovementContentEntry.TABLE_NAME}_${TempMovementContentEntry.EAN}"
        )
    ]
)
data class TempMovementContentEntity(
    @ColumnInfo(name = TempMovementContentEntry.WAREHOUSE_MOVEMENT_ID) var warehouseMovementId: Long,
    @PrimaryKey @ColumnInfo(name = TempMovementContentEntry.ID) var id: Long,
    @ColumnInfo(name = TempMovementContentEntry.CONTENT_STATUS) val contentStatus: Int,
    @ColumnInfo(name = TempMovementContentEntry.ASSET_ID) val assetId: Long,
    @ColumnInfo(name = TempMovementContentEntry.CODE) val code: String,
    @ColumnInfo(name = TempMovementContentEntry.DESCRIPTION) val description: String,
    @ColumnInfo(name = TempMovementContentEntry.STATUS, defaultValue = "1") val status: Int,
    @ColumnInfo(name = TempMovementContentEntry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long,
    @ColumnInfo(name = TempMovementContentEntry.LABEL_NUMBER) val labelNumber: Int?,
    @ColumnInfo(name = TempMovementContentEntry.PARENT_ID) val parentId: Long?,
    @ColumnInfo(name = TempMovementContentEntry.QTY) val qty: Double?,
    @ColumnInfo(name = TempMovementContentEntry.WAREHOUSE_AREA_STR) val warehouseAreaStr: String,
    @ColumnInfo(name = TempMovementContentEntry.WAREHOUSE_STR) val warehouseStr: String,
    @ColumnInfo(name = TempMovementContentEntry.ITEM_CATEGORY_ID, defaultValue = "0") val itemCategoryId: Long,
    @ColumnInfo(name = TempMovementContentEntry.ITEM_CATEGORY_STR) val itemCategoryStr: String,
    @ColumnInfo(name = TempMovementContentEntry.OWNERSHIP_STATUS, defaultValue = "1") val ownershipStatus: Int,
    @ColumnInfo(name = TempMovementContentEntry.MANUFACTURER) val manufacturer: String?,
    @ColumnInfo(name = TempMovementContentEntry.MODEL) val model: String?,
    @ColumnInfo(name = TempMovementContentEntry.SERIAL_NUMBER) val serialNumber: String?,
    @ColumnInfo(name = TempMovementContentEntry.EAN) val ean: String?
) {
    constructor(content: WarehouseMovementContent) : this(
        warehouseMovementId = content.warehouseMovementId,
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