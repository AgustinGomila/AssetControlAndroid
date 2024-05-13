package com.dacosys.assetControl.data.room.entity.movement

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent.Entry
import java.math.BigDecimal

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.WAREHOUSE_MOVEMENT_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_MOVEMENT_ID}"),
        Index(value = [Entry.ASSET_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_ID}"),
        Index(value = [Entry.CODE], name = "IDX_${Entry.TABLE_NAME}_${Entry.CODE}")
    ]
)
data class WarehouseMovementContent(
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_ID) val warehouseMovementId: Long,
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.ASSET_ID) val assetId: Long?,
    @ColumnInfo(name = Entry.CODE) val code: String,
    @ColumnInfo(name = Entry.QTY) val qty: BigDecimal?
) {
    object Entry {
        const val TABLE_NAME = "warehouse_movement_content"
        const val ID = "_id"
        const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
        const val ASSET_ID = "asset_id"
        const val CODE = "code"
        const val QTY = "qty"
    }
}